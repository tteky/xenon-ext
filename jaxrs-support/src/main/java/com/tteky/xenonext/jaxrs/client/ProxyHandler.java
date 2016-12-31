package com.tteky.xenonext.jaxrs.client;

import com.tteky.xenonext.jaxrs.reflect.MethodInfo;
import com.tteky.xenonext.jaxrs.reflect.MethodInfoBuilder;
import com.tteky.xenonext.jaxrs.reflect.ParamMetadata;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceRequestSender;
import com.vmware.xenon.common.Utils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.vmware.xenon.common.UriUtils.extendUri;
import static com.vmware.xenon.common.UriUtils.extendUriWithQuery;

/**
 * Handles the method invocation on contract interfaces.
 * Capable of handling only methods annotated with JAX-RS annotations.
 * Convert jax-rs method invocation to operation and post back results
 */
public class ProxyHandler implements InvocationHandler {

    private Class<?> resourceInterface;
    private String referrer = "/jaxrs/xenon/client";
    private List<MethodInfo> httpMethods = Collections.emptyList();
    private Map<String, Class<?>> typeResolution = Collections.emptyMap();
    private Supplier<URI> baseUriSupplier;
    private ServiceRequestSender client;
    private BiFunction<MethodInfo, Object[], Operation> opBuilder = this::buildOperation;
    private BiConsumer<Pair<Operation, Throwable>, CompletableFuture> errorHandler = this::defaultErrorHandler;
    private BiFunction<Operation, MethodInfo, Object> responseDecoder = this::operationDecoder;
    private OperationInterceptor interceptor = new OperationInterceptor() {
    };

    void init() {
        this.httpMethods = MethodInfoBuilder.parseInterfaceForJaxRsInfo(resourceInterface, typeResolution);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) {
            Class<?> declaringClass = method.getDeclaringClass();
            Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.setAccessible(true);
            MethodHandles.Lookup lookup = (MethodHandles.Lookup) field.get(null);
            return lookup.unreflectSpecial(method, declaringClass)
                    .bindTo(proxy)
                    .invokeWithArguments(args);
        }
        // get the interface describing the resource
        Class<?> proxyIfc = proxy.getClass().getInterfaces()[0];
        if (proxyIfc.equals(resourceInterface)) {
            Optional<MethodInfo> first = this.httpMethods.stream()
                    .filter(httpMethod -> httpMethod.getMethod().equals(method))
                    .findFirst();
            MethodInfo methodInfo = first.orElseGet(() ->
                    MethodInfoBuilder.generateMethodInfo(new Method[]{method}, typeResolution).get(0)
            );
            Operation op = opBuilder.apply(methodInfo, args);
            op = interceptor.interceptBeforeComplete(op);
            CompletableFuture<Object> executeOp = executeOp(methodInfo, op);
            if (methodInfo.isAsyncApi()) {
                return executeOp;
            } else {
                return executeOp.get();
            }
        } else {
            throw new IllegalStateException("Proxy interface is not same as service interface");
        }
    }

    private CompletableFuture<Object> executeOp(MethodInfo httpMethod, Operation op) throws Throwable {
        CompletableFuture<Operation> future = new CompletableFuture<>();
        op.setCompletion(((completedOp, failure) -> {
            try {
                Pair<Operation, Throwable> result = interceptor.interceptAfterComplete(op, Pair.of(completedOp, failure));
                if (result.getRight() == null)
                    future.complete(result.getLeft());
                else {
                    errorHandler.accept(result, future);
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }));
        op.sendWith(client);
        return future.thenApply(completedOp -> getValidReturnValue(completedOp, httpMethod));
    }

    private void defaultErrorHandler(Pair<Operation, Throwable> completed, CompletableFuture<Object> response) {
        response.completeExceptionally(completed.getRight());
    }

    private Object getValidReturnValue(Operation completedOp, MethodInfo httpMethod) {
        Class<?> returnType = httpMethod.getReturnType();
        if (returnType == Void.TYPE) {
            return Void.TYPE;
        } else if (completedOp.hasBody()) {
            return responseDecoder.apply(completedOp, httpMethod);
        } else if (List.class.isAssignableFrom(returnType)) {
            return Collections.emptyList();
        } else if (Set.class.isAssignableFrom(returnType)) {
            return Collections.emptySet();
        } else if (Map.class.isAssignableFrom(returnType)) {
            return Collections.emptyMap();
        } else {
            return null;
        }
    }

    Object operationDecoder(Operation completedOp, MethodInfo httpMethod) {
        if (httpMethod.getType() instanceof ParameterizedType) {
            String json = Utils.toJson(completedOp.getBodyRaw());
            return Utils.fromJson(json, httpMethod.getType());
        } else {
            return completedOp.getBody(httpMethod.getReturnType());
        }
    }


    Operation buildOperation(MethodInfo httpMethod, Object[] args) {
        String methodUri = httpMethod.getUriPath();
        List<String> queryUri = new ArrayList<>();
        Operation op = new Operation();
        if (op.getCookies() == null) {
            op.setCookies(new HashMap<>());
        }
        for (ParamMetadata paramMetadata : httpMethod.getParameters()) {
            if (args[paramMetadata.getParameterIndex()] == null) {
                continue;
            }
            String paramValue = String.valueOf(args[paramMetadata.getParameterIndex()]);
            if (paramMetadata.getType() == ParamMetadata.Type.PATH) {
                String regex = Pattern.quote("{" + paramMetadata.getName() + "}");
                methodUri = methodUri.replaceAll(regex, paramValue);
            } else if (paramMetadata.getType() == ParamMetadata.Type.QUERY) {
                queryUri.add(paramMetadata.getName());
                queryUri.add(paramValue);
            } else if (paramMetadata.getType() == ParamMetadata.Type.BODY) {
                op.setBody(args[paramMetadata.getParameterIndex()]);
            } else if (paramMetadata.getType() == ParamMetadata.Type.HEADER) {
                op.addRequestHeader(paramMetadata.getName(), paramValue);
            } else if (paramMetadata.getType() == ParamMetadata.Type.COOKIE) {
                op.getCookies().put(paramMetadata.getName(), paramValue);
            }
        }
        op.setUri(extendUriWithQuery(extendUri(baseUriSupplier.get(), methodUri), queryUri.toArray(new String[]{})));
        op.setAction(httpMethod.getAction());
        op.setReferer(referrer);
        return op;
    }

    void setResourceInterface(Class<?> resourceInterface) {
        this.resourceInterface = resourceInterface;
    }

    void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    void setTypeResolution(Map<String, Class<?>> typeResolution) {
        this.typeResolution = typeResolution;
    }

    void setOpBuilder(BiFunction<MethodInfo, Object[], Operation> opBuilder) {
        this.opBuilder = opBuilder;
    }

    void setBaseUriSupplier(Supplier<URI> baseUriSupplier) {
        this.baseUriSupplier = baseUriSupplier;
    }

    void setClient(ServiceRequestSender client) {
        this.client = client;
    }

    void setErrorHandler(BiConsumer<Pair<Operation, Throwable>, CompletableFuture> errorHandler) {
        this.errorHandler = errorHandler;
    }

    void setResponseDecoder(BiFunction<Operation, MethodInfo, Object> responseDecoder) {
        this.responseDecoder = responseDecoder;
    }

    void setInterceptor(OperationInterceptor interceptor) {
        this.interceptor = interceptor;
    }
}
