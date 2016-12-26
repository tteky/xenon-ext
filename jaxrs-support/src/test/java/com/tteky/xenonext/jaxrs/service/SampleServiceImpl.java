package com.tteky.xenonext.jaxrs.service;

import com.tteky.xenonext.jaxrs.EmployeePojo;
import com.tteky.xenonext.jaxrs.annotation.OperationBody;
import com.tteky.xenonext.jaxrs.annotation.PATCH;
import com.vmware.xenon.common.Operation;

import javax.ws.rs.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.vmware.xenon.common.Utils.toJson;

/**
 * Created by mageshwaranr on 8/17/2016.
 */
public class SampleServiceImpl extends JaxRsBridgeStatelessService implements SampleService {

    public SampleServiceImpl() {
        setContractInterface(SampleService.class);
    }

    @Path("/simple")
    @GET
    public void simpleGet(Operation get) {
        Map<String, String> help = new LinkedHashMap<>();
        help.put("result", "success");
        get.setBody(help);
        get.complete();
    }

    @Path("/simple")
    @PATCH
    public void simplePatch(Operation get) {
        Map<String, String> help = new LinkedHashMap<>();
        help.put("result", "success");
        get.setBody(help);
        get.complete();
    }

    @Path("/path/{pathParam}/query")
    @POST
    public void postWithQueryAndPath(final @PathParam("pathParam") String pathValue,
                                     final @QueryParam("queryParam") String query,
                                     final @OperationBody List<String> payload,
                                     final Operation get) {
        Map<String, String> response = prepareResponse(pathValue, query, toJson(payload));
        get.setBody(response);
        get.complete();
    }

    @Override
    public CompletableFuture<Map<String, String>> asyncGetWithQueryAndPath(String pathValue, String query) {
        CompletableFuture<Map<String, String>> result = new CompletableFuture<>();
        result.complete(prepareResponse(pathValue, query, null));
        return result;
    }

    @Override
    public Map<String, String> getWithQueryAndPathAndReturn(String pathValue, String query) {
        return prepareResponse(pathValue, query, null);
    }

    @Override
    public CompletableFuture<Map<String, String>> putWithQueryAndPathAndReturn(String pathValue, String query, List<String> payload) {
        CompletableFuture<Map<String, String>> result = new CompletableFuture<>();
        result.complete(prepareResponse(pathValue, query, toJson(payload)));
        return result;
    }

    @Override
    public Map<String, String> postWithModelValidationOnBody(String pathValue, String query, EmployeePojo payload) {
        return prepareResponse(pathValue, query, toJson(payload));
    }

    Map<String, String> prepareResponse(String pathValue, String query, String payload) {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("result", "success");
        response.put("pathParam", pathValue);
        response.put("queryParam", query);
        if (payload != null)
            response.put("body", payload);
        return response;
    }


}
