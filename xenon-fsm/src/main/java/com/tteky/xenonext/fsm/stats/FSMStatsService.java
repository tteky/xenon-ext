package com.tteky.xenonext.fsm.stats;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.tteky.xenonext.fsm.stats.FSMStatsService.SELF_LINK;

/**
 * Stats service
 */
@Path(SELF_LINK)
public interface FSMStatsService {
    String SELF_LINK = "/fsm/monitoring";

    @Path("/list")
    @GET
    Collection<String> allServices();

    @Path("/register")
    @GET
    Collection<String> registerSvc(@QueryParam("uri") String uri, @QueryParam("className") String className);

    @Path("/svc/graphson")
    @GET
    Map<String,String> svcGraphson(@QueryParam("uri") String uri);


    @Path("/doc/graphson")
    @GET
    CompletableFuture<Map<String,String>> docGraphson(@QueryParam("uri") String uri);

}
