package com.tteky.xenonext.jaxrs.client;

import com.tteky.xenonext.jaxrs.reflect.MethodInfo;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.ServiceClient;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.common.http.netty.NettyHttpServiceClient;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.vmware.xenon.common.UriUtils.buildUri;
import static com.vmware.xenon.common.UriUtils.extendUri;
import static com.vmware.xenon.common.Utils.DEFAULT_THREAD_COUNT;
import static java.util.Objects.requireNonNull;

/**
 * Created by mageshwaranr
 */
public class JaxRsServiceClient {
    private Class<?> resourceInterface;
    private ServiceHost host;
    private ServiceClient serviceClient;
    private OperationInterceptor interceptor;
    private Map<String, Class<?>> typeResolution = null;
    private Supplier<URI> baseUriSupplier;
    private URI baseUri;
    private BiFunction<MethodInfo, Object[], Operation> opBuilder;
    private BiConsumer<Pair<Operation, Throwable>, CompletableFuture> errorHandler;
    private BiFunction<Operation, MethodInfo, Object> responseDecoder;

    private Logger log = LoggerFactory.getLogger(getClass());

    public static JaxRsServiceClient newBuilder() {
        return new JaxRsServiceClient();
    }

    public JaxRsServiceClient withResourceInterface(Class<?> resourceInterface) {
        this.resourceInterface = resourceInterface;
        return this;
    }

    public JaxRsServiceClient withHost(ServiceHost host) {
        this.host = host;
        return this;
    }

    public JaxRsServiceClient withBaseUri(String baseUri) {
        this.baseUri = buildUri(baseUri);
        return this;
    }

    public JaxRsServiceClient withBaseUri(Supplier<URI> baseUriSupplier) {
        this.baseUriSupplier = baseUriSupplier;
        return this;
    }

    public JaxRsServiceClient withBaseUri(URI baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    public JaxRsServiceClient withInterceptor(OperationInterceptor interceptor) {
        this.interceptor = interceptor;
        return this;
    }

    public JaxRsServiceClient withGenericTypeResolution(String typeName, Class<?> type) {
        if (typeResolution == null) {
            typeResolution = new HashMap<>();
        }
        this.typeResolution.put(typeName, type);
        return this;
    }

    public JaxRsServiceClient withErrorHandler(BiConsumer<Pair<Operation, Throwable>, CompletableFuture> errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public JaxRsServiceClient withOperationBuilder(BiFunction<MethodInfo, Object[], Operation> opBuilder) {
        this.opBuilder = opBuilder;
        return this;
    }

    public JaxRsServiceClient withResponseDecoder(BiFunction<Operation, MethodInfo, Object> responseDecoder) {
        this.responseDecoder = responseDecoder;
        return this;
    }

    public <C> C build() {
        requireNonNull(resourceInterface, "Interface capturing the Service API is required");
        // need baseUriSupplier
        if (baseUri == null && baseUriSupplier == null) {
            requireNonNull(host, "Base URI is required");
            baseUri = host.getUri();
        }
        if (host == null) {
            log.warn("Need Xenon service host used to bootstrap the process. Proceeding with default service client sender");
            serviceClient = createServiceClient();
        } else {
            serviceClient = host.getClient();
        }

        if (baseUriSupplier == null) {
            baseUriSupplier = () -> addPathFromAnnotation(resourceInterface, baseUri);
        }
        ProxyHandler proxyHandler = new ProxyHandler();
        proxyHandler.setResourceInterface(resourceInterface);
        proxyHandler.setBaseUriSupplier(baseUriSupplier);
        proxyHandler.setClient(serviceClient);
        if (host != null)
            proxyHandler.setReferrer(host.getPublicUriAsString());
        if (this.errorHandler != null)
            proxyHandler.setErrorHandler(this.errorHandler);
        if (this.responseDecoder != null)
            proxyHandler.setResponseDecoder(responseDecoder);
        if (this.opBuilder != null)
            proxyHandler.setOpBuilder(opBuilder);
        if (typeResolution != null)
            proxyHandler.setTypeResolution(this.typeResolution);
        if (interceptor != null)
            proxyHandler.setInterceptor(interceptor);

        proxyHandler.init();

        return (C) Proxy.newProxyInstance(resourceInterface.getClassLoader(), new Class[]{resourceInterface}, proxyHandler);
    }

    private static URI addPathFromAnnotation(AnnotatedElement element, URI parent) {
        String pathToBeAdded = parsePath(element);
        return extendUri(parent, pathToBeAdded);
    }


    private static String parsePath(AnnotatedElement element) {
        Path path = element.getAnnotation(Path.class);
        if (path != null) {
            return path.value();
        }
        return null;
    }

    public static ServiceClient createServiceClient() {
        try {
            ServiceClient serviceClient = NettyHttpServiceClient.create(
                    JaxRsServiceClient.class.getSimpleName(),
                    Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT),
                    Executors.newScheduledThreadPool(DEFAULT_THREAD_COUNT));
            serviceClient.start();
            return serviceClient;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create ServiceClient  due to " + e.getMessage(), e);
        }
    }

}
