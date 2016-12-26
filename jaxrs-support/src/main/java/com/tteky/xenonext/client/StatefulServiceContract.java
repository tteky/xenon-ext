package com.tteky.xenonext.client;

import com.tteky.xenonext.jaxrs.annotation.OperationBody;
import com.tteky.xenonext.jaxrs.annotation.PATCH;

import javax.ws.rs.*;
import java.util.concurrent.CompletableFuture;

import static com.tteky.xenonext.client.ServiceClientUtil.selfLinkToId;

/**
 * Created by mages_000 on 26-Dec-16.
 */
public interface StatefulServiceContract<T> {

    @GET
    @Path("{id}")
    CompletableFuture<T> get(@PathParam("id") String id);

    default CompletableFuture<T> getBySelfLink(String docLink) {
        return get(selfLinkToId(docLink));
    }

    @POST
    CompletableFuture<T> post(@OperationBody T postBody);

    @PATCH
    @Path("{id}")
    CompletableFuture<T> patch(@PathParam("id") String id, @OperationBody T patchBody);

    default CompletableFuture<T> patchBySelfLink(String docLink, T patchBody) {
        return patch(selfLinkToId(docLink), patchBody);
    }

    @PUT
    @Path("{id}")
    CompletableFuture<T> put(@PathParam("id") String id, @OperationBody T putBody);

    default CompletableFuture<T> putBySelfLink(String docLink, T patchBody) {
        return put(selfLinkToId(docLink), patchBody);
    }

    @DELETE
    @Path("{id}")
    CompletableFuture<T> delete(@PathParam("id") String id, @OperationBody T deleteBody);

}

