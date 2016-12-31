package com.tteky.xenonext.client;

import com.tteky.xenonext.jaxrs.annotation.OperationBody;
import com.vmware.xenon.common.Utils;
import com.vmware.xenon.services.common.QueryTask;

import javax.ws.rs.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * Contract for Xenon core query tasks and odata query
 */
@Path("/core")
public interface QueryContract {

    String LE = " le ", OR = " or ", ANY = " any ", ALL = " all ", AND = " and ";
    String GT = " gt ", GE = " ge ", lt = " lt ", EQ = " eq ", NE = " ne ";

    @Path("/odata-queries")
    @GET
    CompletableFuture<QueryTask> oDataQuery(@QueryParam("$filter") String filterCriteria);

    @Path("/query-tasks/{id}")
    @GET
    CompletableFuture<QueryTask> getQueryResults(@PathParam("id") String id);

    @Path("/query-tasks")
    @POST
    CompletableFuture<QueryTask> query(@OperationBody QueryTask filterCriteria);

    /**
     * TO be used only with queries returning homogeneous results of a single type
     * @param filterCriteria the actual criteria
     * @param clazz the return type, usually it will be an array type
     * @param <T>
     * @return
     */
    default <T> CompletableFuture<T> typedODataQuery(String filterCriteria, Class<T> clazz) {
        return oDataQuery(filterCriteria)
                .thenApply(task -> {
                    Map<String, Object> documents = task.results.documents;
                    return Utils.fromJson(documents.values(), clazz);
                });
    }

    /**
     * To be used only with direct task queries returning homogeneous results of a single type
     * @param filterCriteria the actual criteria
     * @param clazz the return type, usually it will be an array type
     * @param <T>
     * @return
     */
    default <T> CompletableFuture<T> typedQuery(QueryTask filterCriteria, Class<T> clazz) {
        return query(filterCriteria)
                .thenApply(task -> {
                    Map<String, Object> documents = task.results.documents;
                    return Utils.fromJson(documents.values(), clazz);
                });
    }

}
