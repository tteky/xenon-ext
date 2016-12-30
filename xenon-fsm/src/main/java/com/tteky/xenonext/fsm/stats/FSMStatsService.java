package com.tteky.xenonext.fsm.stats;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.tteky.xenonext.fsm.stats.FSMStatsService.SELF_LINK;

/**
 * Created by mages_000 on 29-Dec-16.
 */
@Path(SELF_LINK)
public interface FSMStatsService {
    String SELF_LINK = "/fsm/monitoring";

    @Path("/list")
    @GET
    List<String> allServices();

    @Path("/svc/graphson")
    @GET
    Map<String,String> svcGraphson(@QueryParam("uri") String uri);


    @Path("/doc/graphson")
    @GET
    CompletableFuture<Map<String,String>> docGraphson(@QueryParam("uri") String uri);

}
