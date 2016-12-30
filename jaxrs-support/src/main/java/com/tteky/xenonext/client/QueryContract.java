package com.tteky.xenonext.client;

import com.tteky.xenonext.jaxrs.annotation.OperationBody;
import com.vmware.xenon.common.Utils;
import com.vmware.xenon.services.common.QueryTask;

import javax.ws.rs.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


/**
 * Created by mages_000 on 29-Dec-16.
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

    default <T> CompletableFuture<T> typedODataQuery(String filterCriteria, Class<T> clazz) {
        return oDataQuery(filterCriteria)
                .thenApply(task -> {
                    Map<String, Object> documents = task.results.documents;
                    return Utils.fromJson(documents.values(), clazz);
                });
    }

    default <T> CompletableFuture<T> typedQuery(QueryTask filterCriteria, Class<T> clazz) {
        return query(filterCriteria)
//                .thenCompose( task -> getQueryResults(selfLinkToId(task.documentSelfLink)))
                .thenApply(task -> {
                    Map<String, Object> documents = task.results.documents;
                    return Utils.fromJson(documents.values(), clazz);
                });
    }

}
