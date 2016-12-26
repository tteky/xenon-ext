package com.tteky.xenonext.jaxrs.service;

import com.tteky.xenonext.jaxrs.EmployeePojo;
import com.tteky.xenonext.jaxrs.annotation.OperationBody;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by mages_000 on 26-Dec-16.
 */
public interface SampleService {

    String SELF_LINK = "/vrbc/common/routing/test";

    @Path("/path/{pathParam}/query")
    @GET
    CompletableFuture<Map<String, String>> asyncGetWithQueryAndPath(final @PathParam("pathParam") String pathValue,
                                                                    final @QueryParam("queryParam") String query);


    @Path("/complete/path/{pathParam}")
    @GET
    Map<String, String> getWithQueryAndPathAndReturn(@PathParam("pathParam") String pathValue,
                                                     @QueryParam("queryParam") String query);


    @Path("/complete/path/{pathParam}")
    @PUT
    CompletableFuture<Map<String, String>> putWithQueryAndPathAndReturn(final @PathParam("pathParam") String pathValue,
                                                                        final @QueryParam("queryParam") String query,
                                                                        final @OperationBody List<String> payload);


    @Path("/validation/path/{pathParam}")
    @POST
    Map<String, String> postWithModelValidationOnBody(final @PathParam("pathParam") String pathValue,
                                                      final @QueryParam("queryParam") String query,
                                                      final @OperationBody EmployeePojo payload);


}
