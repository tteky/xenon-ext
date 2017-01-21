package com.tteky.xenonext.util.feature;

import com.tteky.xenonext.jaxrs.annotation.OperationBody;
import com.vmware.xenon.common.ServiceHost;
import com.vmware.xenon.common.StatelessService;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static com.tteky.xenonext.util.feature.FeatureService.SELF_LINK;

/**
 * Created by mageshwaranr on 02-Jan-17.
 */
@Path(SELF_LINK)
public interface FeatureService {

    String SELF_LINK = "/common/feature";

    static StatelessService create(ServiceHost host) {
        host.startFactory(new FeatureDao());
        FeatureServiceImpl service = new FeatureServiceImpl();
        host.startService(service);
        return service;
    }

    @GET
    CompletableFuture<Collection<Feature>> listAll();

    @POST
    CompletableFuture<Feature> newFeature(@OperationBody Feature feature);

    @GET
    @Path("/find/{group}/{name}")
    CompletableFuture<Feature> findByGroupAndName(@PathParam("group") String group, @PathParam("name") String name);

    default CompletableFuture<Feature> findFeature(Feature feature) {
        return findByGroupAndName(feature.group, feature.name);
    }

    // using GET now because patch,post enforces body
    @GET
    @Path("/enable/{group}/{name}")
    CompletableFuture<Feature> enableFeature(@PathParam("group") String group, @PathParam("name") String name);

    // using GET now because patch,post enforces body
    @GET
    @Path("/disable/{group}/{name}")
    CompletableFuture<Feature> disableFeature(@PathParam("group") String group, @PathParam("name") String name);


}
