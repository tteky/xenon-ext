package com.tteky.xenonext.jaxrs.service;

import com.tteky.xenonext.jaxrs.reflect.MethodInfo;
import com.tteky.xenonext.jaxrs.reflect.ParamMetadata;
import com.tteky.xenonext.util.HttpError;
import com.tteky.xenonext.util.HttpErrorResponse;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static com.vmware.xenon.common.UriUtils.*;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;


/**
 * Created by mageshwaranr on 8/17/2016.
 * For internal consumption only
 * Responsible for processing Operation to fetch query & path parameters and to invoke the actual method
 */
class RoutingOperationHandler implements Consumer<Operation> {

    private Logger log = LoggerFactory.getLogger(getClass());


    private final String path;
    private final MethodInfo httpMethod;
    private final Service service;
    /**
     * Method receives Operation as an argument. When operation completes will be handled by the method itself
     */
    boolean hasOperationAsAnArgument = false;
    /**
     * holds true for non-void methods
     */
    boolean hasValidReturnType = false;
    private Validator validator;
    private int resourcePathOffset;

    RoutingOperationHandler(String path, MethodInfo publicMethod, Service service) {
        this.path = path;
        this.httpMethod = publicMethod;
        this.service = service;
    }

    void init() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        hasOperationAsAnArgument = httpMethod.getParameters().stream().anyMatch(paramMetadata -> paramMetadata.getType().equals(ParamMetadata.Type.OPERATION));
        hasValidReturnType = !httpMethod.getMethod().getReturnType().equals(Void.TYPE);
        // http method provides path index using @PATH at method level only. The service class will have the path prefix.
        // this offset is to capture the no of forward slashes at service level
        resourcePathOffset = path.split(URI_PATH_CHAR).length - (httpMethod.getUriPath() == null ? 0 : httpMethod.getUriPath().split(URI_PATH_CHAR).length);
    }

    @Override
    public void accept(Operation operation) {
        try {
            doLogging(operation);
            Map<String, String> queryParams = parseUriQueryParams(operation.getUri());
            Map<Integer, String> pathValues = parsePathValues(operation.getUri());
            Object[] methodInputs;
            try {
                methodInputs = findMethodInputs(operation, queryParams, pathValues);
            } catch (HttpError error) {
                operation.fail(error, HttpErrorResponse.from(error));
                return;  // model failure occurred. Skip actual API call
            }
            if (hasOperationAsAnArgument) {
                // do not invoke operation.complete(). User takes care of this.
                invokeMethodAndSetBody(operation, methodInputs);
            } else {
                // we have to invoke operation.complete() or operation.fail() in all possible scenarios
                if (httpMethod.isAsyncApi()) {
                    invokeMethodAndHandleOperationAsync(operation, methodInputs);
                } else {
                    invokeMethodAndHandleOperationSync(operation, methodInputs);
                }
            }
        } catch (Exception e) { // handles exception in synchronous invocation / method execution
            log.error("Unable to invoke the " + this.httpMethod.getName(), e);
            operation.fail(e, format("Failed to execute %s handler on %s ", operation.getAction(), operation.getUri().getPath()));
        }

    }

    private void doLogging(Operation operation) {
        long startTime = System.nanoTime();
        log.trace("Performing {} on {}", operation.getAction(), operation.getUri());
        operation.nestCompletion((completedOp, failure) -> {
            if (failure == null) {
                log.debug("Operation {} on {} Succeeded. It took {}",
                        operation.getAction(), operation.getUri(), System.nanoTime() - startTime);
                operation.complete();
            } else {
                log.info("Operation {} on {} failed. It took {}",
                        operation.getAction(), operation.getUri(), System.nanoTime() - startTime);
                operation.fail(failure);
            }
        });
    }


    /**
     * Finds the actual arguments to be passed to the method during invocation
     *
     * @param operation   Operation from which request body needs to be extracted
     * @param queryParams all the query params required by the method
     * @param pathValues  all the path params required by the method
     * @return method parameters
     * @throws HttpError if method body type has validation annotations and model breaches constraints
     */
    private Object[] findMethodInputs(Operation operation, Map<String, String> queryParams, Map<Integer, String> pathValues) throws HttpError {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        Map<String, String> cookies = operation.getCookies() == null ? emptyMap() : operation.getCookies();
        Object[] arguments = httpMethod.getParameters().stream()
                .map(paramMetadata -> {
                    switch (paramMetadata.getType()) {

                        case QUERY:
                            return queryParams.get(paramMetadata.getName());

                        case HEADER:
                            return operation.getRequestHeader(paramMetadata.getName());

                        case COOKIE:
                            return cookies.get(paramMetadata.getName());

                        case PATH:
                            // -1 so that length gets converted to index
                            Integer index = httpMethod.getPathParamsVsUriIndex().getOrDefault(paramMetadata.getName(), -1);
                            return pathValues.get(index + resourcePathOffset);

                        case OPERATION:
                            return operation;

                        case BODY:
                            Object body = operation.getBody(paramMetadata.getParamterType());
                            violations.addAll(validator.validate(body));
                            return body;

                        default:
                            return null;
                    }
                }).toArray();

        if (!violations.isEmpty()) {
            log.warn("Operation {} on {} has constraints violations. Rejecting the request", operation.getAction(), operation.getUri());
            violations.forEach(violation -> log.warn("Invalid Value {}, Violation Message {} ", violation.getInvalidValue(), violation.getMessage()));
            HttpError error = new HttpError(400, "Constraint violations occurred while validating input request");
            Map<String, Object> context = new HashMap<>();
            context.put("Constraints", violations);
            error.setContext(context);
            throw error;
        }
        return arguments;
    }


    private void invokeMethodAndSetBody(Operation operation, Object[] methodInputs) throws Exception {
        Object invocationResult = this.httpMethod.getMethod().invoke(service, methodInputs);
        if (hasValidReturnType) {
            operation.setBody(invocationResult);
        }
    }

    private void invokeMethodAndHandleOperationSync(Operation operation, Object[] methodInputs) throws Exception {
        Object invocationResult = this.httpMethod.getMethod().invoke(service, methodInputs);
        if (hasValidReturnType) {
            operation.setBody(invocationResult);
        }
        operation.complete();
    }

    private void invokeMethodAndHandleOperationAsync(Operation operation, Object[] methodInputs) throws Exception {
        Object invocationResult = this.httpMethod.getMethod().invoke(service, methodInputs);
        CompletableFuture<?> future = (CompletableFuture<?>) invocationResult;
        future.exceptionally(throwable -> {
            if (throwable instanceof CompletionException ||
                    throwable instanceof ExecutionException) {
                operation.fail(throwable.getCause(), HttpErrorResponse.from(throwable.getCause()));
            } else {
                operation.fail(throwable, HttpErrorResponse.from(throwable));
            }
            return null;
        });
        // this won't be invoked if future got completed exceptionally
        future.thenAccept(resp -> {
            operation.setBody(resp);
            operation.complete();
        });

    }


    private static Map<Integer, String> parsePathValues(URI uri) {
        Map<Integer, String> pathValues = new HashMap<>();
        String path = normalizeUriPath(uri.getPath());
        String[] tokens = path.split(URI_PATH_CHAR);
        for (int i = 0; i < tokens.length; i++) {
            pathValues.put(i, tokens[i]);
        }
        return pathValues;
    }

}
