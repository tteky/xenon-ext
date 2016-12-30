package com.tteky.xenonext.client;

import com.tteky.xenonext.jaxrs.client.JaxRsServiceClient;
import com.vmware.xenon.common.BasicReusableHostTestCase;
import com.vmware.xenon.services.common.ExampleService;
import com.vmware.xenon.services.common.QueryTask;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static com.tteky.xenonext.client.ServiceClientUtil.newStatefulSvcContract;
import static com.tteky.xenonext.client.ServiceClientUtil.waitForServiceAvailability;
import static com.vmware.xenon.services.common.ExampleService.ExampleServiceState;
import static org.junit.Assert.assertEquals;

/**
 * Created by mages_000 on 26-Dec-16.
 */
public class ODataQueryTest extends BasicReusableHostTestCase {

    static StatefulServiceContract<ExampleService.ExampleServiceState> testSvc;
    static QueryContract querySvc;

    @Before
    public void initializeHost() throws Throwable {
        host.startFactory(new ExampleService());
        waitForServiceAvailability(host, ExampleService.FACTORY_LINK);
        testSvc = newStatefulSvcContract(host, ExampleService.FACTORY_LINK,
                ExampleService.ExampleServiceState.class);
        querySvc = JaxRsServiceClient.newBuilder()
                .withHost(host)
                .withResourceInterface(QueryContract.class)
                .build();
    }

    @Test
    public void testODataQueryExecution() throws Throwable {
        setupData();
        testODataQuery();
        testODataQueryContract();
        testTaskQueryContract();
    }

    private void testODataQuery() throws InterruptedException, java.util.concurrent.ExecutionException {
        CompletableFuture<ExampleServiceState[]> execute = ODataQuery.newInstance()
                .withHost(host)
                .withFilterCriteria("name " + ODataQuery.EQ + " Diya")
                .execute(ExampleServiceState[].class);
        ExampleServiceState[] states = execute.get();
        verifyMatch(states);
    }

    private void testODataQueryContract() throws Throwable {
        ExampleServiceState[] states = querySvc.typedODataQuery("name " + ODataQuery.EQ + " Diya", ExampleServiceState[].class).get();
        verifyMatch(states);
    }

    private void verifyMatch(ExampleServiceState[] states) {
        assertEquals(1, states.length);
        assertEquals("Diya", states[0].name);
        assertEquals("Some mandatory value", states[0].required);
    }

    private void testTaskQueryContract() throws Throwable {
        QueryTask.Query query = QueryTask.Query.Builder.create()
                .addKindFieldClause(ExampleServiceState.class)
                .addFieldClause("name", "Diya")
                .build();
        QueryTask queryTask = QueryTask.Builder.createDirectTask()
                .addOption(QueryTask.QuerySpecification.QueryOption.EXPAND_CONTENT)
                .addOption(QueryTask.QuerySpecification.QueryOption.INCLUDE_ALL_VERSIONS)
                .setQuery(query).build();

        ExampleServiceState[] states = querySvc.typedQuery(queryTask, ExampleServiceState[].class).get();
        verifyMatch(states);
    }


    private void setupData() throws Throwable {
        ExampleServiceState state = new ExampleServiceState();
        state.name = "Ram";
        state.required = "Some mandatory value";
        testSvc.post(state).get();

        state = new ExampleServiceState();
        state.name = "Raj";
        state.required = "Some mandatory value";
        testSvc.post(state).get();

        state = new ExampleServiceState();
        state.name = "Diya";
        state.required = "Some mandatory value";
        testSvc.post(state).get();

        state = new ExampleServiceState();
        state.name = "Sita";
        state.required = "Some mandatory value";
        testSvc.post(state).get();
    }

}