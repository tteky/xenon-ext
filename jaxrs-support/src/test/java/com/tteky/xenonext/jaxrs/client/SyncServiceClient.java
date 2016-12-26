package com.tteky.xenonext.jaxrs.client;

import com.tteky.xenonext.jaxrs.SuccessResponse;
import com.tteky.xenonext.jaxrs.annotation.OperationBody;
import com.tteky.xenonext.jaxrs.annotation.PATCH;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;

/**
 * Created by mageshwaranr on 8/22/2016.
 */
@Path("/vrbc/xenon/util/test")
public interface SyncServiceClient {

    @GET
    @Path("/get/{path}")
    Map<String, String> getAction(@QueryParam("query") String query, @PathParam("path") String path);

    @PATCH
    @Path("/patch")
    Map<String, String> patchAction(@OperationBody List<Integer> contents);

    @POST
    @Path("/post")
    Map<String, String> postAction(@OperationBody List<String> contents);


    @GET
    @Path("/get/genericReturn")
    Map<String, SuccessResponse> getActionWithGenericReturn();

    @DELETE
    @Path("/delete/{path}")
    List<String> deleteAction(@PathParam("path") String path);

    @POST
    @Path("/post/auth")
    List<String> postActionWithAuthInfo(@HeaderParam("header") String header,
                                        @CookieParam("cookie") String cookie, @OperationBody List<Integer> contents);

    default String appendDefault(String in) {
        return "Default_" + in;
    }

    @GET
    @Path("/get/generic/type")
    <T> T genericReturnType();

}
