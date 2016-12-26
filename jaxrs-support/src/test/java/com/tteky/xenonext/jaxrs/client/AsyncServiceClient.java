package com.tteky.xenonext.jaxrs.client;

import com.tteky.xenonext.jaxrs.SuccessResponse;
import com.tteky.xenonext.jaxrs.annotation.OperationBody;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by mageshwaranr
 */
@Path("/vrbc/xenon/util/test")
public interface AsyncServiceClient {

    @GET
    @Path("/get/{path}")
    CompletableFuture<Map<String, String>> getAction(@QueryParam("query") String query, @PathParam("path") String path);

    default CompletableFuture<Map<String, String>> invokeGetAction(String query, String path) {
        String val = "Value1";
        return getAction(query + val, path + val);
    }

    @POST
    @Path("/post")
    CompletableFuture<Void> postAction(@OperationBody List<String> contents);


    @POST
    @Path("/post/auth")
    CompletableFuture<List<String>> postActionWithAuthInfo(@HeaderParam("header") String header,
                                                           @CookieParam("cookie") String cookie, @OperationBody List<Integer> contents);

    @GET
    @Path("/get/genericReturn")
    CompletableFuture<Map<String, SuccessResponse>> getActionWithGenericReturn();

}

