package com.tteky.xenonext.jaxrs.service;


import com.tteky.xenonext.jaxrs.reflect.MethodInfo;
import com.tteky.xenonext.jaxrs.reflect.MethodInfoBuilder;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.RequestRouter;
import com.vmware.xenon.common.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.vmware.xenon.common.Service.ServiceOption.URI_NAMESPACE_OWNER;
import static com.vmware.xenon.common.UriUtils.*;


/**
 * Creates com.vmware.xenon.common.RequestRouter to route requests to appropriate methods
 * identified by jax-rs annotations
 * <p>
 * Each public method needs to be annotated with HTTP verb (sub-class of HTTPMethod annotation ie.,
 * GET, POST, PUT, DELETE, PATCH etc.,) and can be annotated with @Path URI relative to the
 * self-link of the service and can make use of @QueryParam and @PathParam to extract values
 * from URI.
 * </p><p>
 * Such methods can have at max one parameter which is not annotated and the parameter type should be Operation.
 * If Operation is received as an argument, then the method is assumed to hold responsibility of setting appropriate
 * response body and invoke operation.complete().
 * </p>
 * If method doesn't gets Operation as an input argument then Operation.complete
 * is invoked automatically. If such method returns non-void type, the return value is set as body.
 * <p>Methods can return CompletableFuture and actual Operation will be completed upon the future completion by the framework</p>
 * <p>
 * Methods which do not have HTTPMethod annotations are skipped. </p>
 */
public class RequestRouterBuilder {

    private static Logger log = LoggerFactory.getLogger(RequestRouterBuilder.class);

    /**
     * IllegalArgumentException if HTTPMethod is not mappable to Xenon Service.Action
     *
     * @param xenonService
     * @return
     */
    public static RequestRouter parseJaxRsAnnotations(Service xenonService, Class<?>... interfaces) {
        if (!xenonService.hasOption(URI_NAMESPACE_OWNER)) {
            throw new IllegalArgumentException("URI_NAMESPACE_OWNER option needs to be enabled");
        }
        List<MethodInfo> httpMethods = MethodInfoBuilder.parseServiceForJaxRsInfo(xenonService.getClass(), Collections.emptyMap());
        RequestRouter router = new RequestRouter();
        registerRoutes(router, xenonService, httpMethods);
        Stream.of(interfaces)
                .filter(Objects::nonNull)
                .filter(iFace -> iFace.isAssignableFrom(xenonService.getClass()))
                .forEach(iFace -> registerRoutes(router, xenonService, MethodInfoBuilder.parseInterfaceForJaxRsInfo(iFace, Collections.emptyMap())));
        return router;
    }

    private static void registerRoutes(RequestRouter router, Service xenonService, List<MethodInfo> httpMethods) {
        httpMethods.forEach(methodInfo -> {
            String path = buildUriPath(xenonService.getSelfLink(), methodInfo.getUriPath());
            Predicate<Operation> predicate = newUriMatcher(path);
            RoutingOperationHandler routingOperationHandler = new RoutingOperationHandler(path, methodInfo, xenonService);
            routingOperationHandler.init();
            router.register(methodInfo.getAction(), predicate, routingOperationHandler, "JaxRs annotation based Router");
            log.info("Registered {} on {} to {}", methodInfo.getAction(), path, methodInfo.getName());
        });
    }


    static Predicate<Operation> newUriMatcher(String pathWithParams) {
        if (pathWithParams.contains("{") && pathWithParams.contains("}")) { //URI with path param
            return new DynamicPathParamMatcher(pathWithParams);
        } else {
            return operation -> normalizeUriPath(operation.getUri().getPath()).equals(pathWithParams);
        }

    }

    private static class DynamicPathParamMatcher implements Predicate<Operation> {

        private String[] expectedPathTokens;

        /**
         * Matches path with path param i.e., curly braces
         *
         * @param pathWithParams Normalized path param
         */
        DynamicPathParamMatcher(String pathWithParams) {
            this.expectedPathTokens = pathWithParams.split(URI_PATH_CHAR);
        }

        @Override
        public boolean test(Operation operation) {
            String path = operation.getUri().getPath();
            String[] actualTokens = path.split(URI_PATH_CHAR);
            if (actualTokens.length != expectedPathTokens.length) {
                return false;
            }


            for (int i = 0; i < actualTokens.length; i++) {
                // if path don't match, it has to be path param
                String pathParam = expectedPathTokens[i];
                if (!pathParam.equals(actualTokens[i])) {
                    if (pathParam.length() > 0
                            && pathParam.charAt(0) == '{' && pathParam.charAt(pathParam.length() - 1) == '}') {
                        continue;
                    }
                    return false;
                }
            }
            return true;
        }


    }


}
