package com.tteky.xenonext.jaxrs.reflect;

import com.tteky.xenonext.jaxrs.SuccessResponse;
import com.tteky.xenonext.jaxrs.annotation.OperationBody;
import com.tteky.xenonext.jaxrs.annotation.PATCH;
import com.vmware.xenon.common.Operation;

import javax.ws.rs.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by mageshwaranr on 8/16/2016.
 * <p>
 * A test service class to facilitate testing JaxRsRouter
 */
public class MockWithJaxRsParams<S1, S2> {

    //used in  testParseAction & testParseJaxRsMethodInfo
    @GET
    public void testGetAction(@QueryParam("query") String query, @PathParam("path") String path, Operation op) {

    }


    @PATCH
    @Path("/patch/{path}")
    public void testPatchAction(@PathParam("path") String path, @OperationBody List<String> contents, Operation op) {

    }

    @DELETE
    @Path("/delete/{path}")
    public List<String> testDeleteAction(@PathParam("path") String path) {
        return null;
    }

    //used in test async API

    @POST
    @Path("/post/{path}")
    public CompletableFuture<List<String>> testPostAction(@PathParam("path") String path, @OperationBody List<String> contents) {
        return null;
    }

    // to test return type

    //used in testExtractParams
    public void methodWithQueryAndPathParam(@QueryParam("query") String query,
                                            @PathParam("path") String path) {

    }


    public void methodWithQueryAndPathParams(@QueryParam("query1") String query1,
                                             @QueryParam("query2") String query2,
                                             @PathParam("path1") String path1,
                                             @PathParam("path2") String path2) {

    }

    public void methodWithQueryParamOnly(@QueryParam("query") String query) {

    }

    public void methodWithPathParamOnly(@PathParam("path") String path) {

    }

    public void methodWithPathParamAndBody(@PathParam("path") String path, @OperationBody Object customObject) {

    }

    public void methodWithQueryAndOperation(@QueryParam("query") String path, Operation op) {

    }

    public void methodWithCookieAndHeader(@CookieParam("cookie") String cookie, @HeaderParam("header") String header) {

    }

    //used in testExtractParamsWithIncorrectConfig

    public void methodWithUnannotatedArgs(@QueryParam("query") String path, Operation op, Object someBody) {

    }

    public void methodWithUnannotatedArgAndIncorrectType(@QueryParam("query") String path, Object someBody) {

    }

    public void methodWithUnsupportedAnnotation(@MatrixParam("matrix") String param, Operation op) {

    }

    // methods to test generic return type
    public Map<String, SuccessResponse> methodWithGenericReturnType() {
        return null;
    }

    public SuccessResponse methodWithReturnType() {
        return null;
    }

    public S1 methodWithGenericDynamicReturnType() {
        return null;
    }

    public CompletableFuture<Map<String, SuccessResponse>> asyncMethodWithGenericReturnType() {
        return null;
    }

    public CompletableFuture<?> asyncMethodWithWildCardReturnType() {
        return null;
    }

    public CompletableFuture<String[]> asyncMethodWithArrayReturnType() {
        return null;
    }

    public CompletableFuture asyncMethodWithNonGenericReturnType() {
        return null;
    }

    public CompletableFuture<S2> asyncMethodWithGenericDynamicReturnType() {
        return null;
    }


}
