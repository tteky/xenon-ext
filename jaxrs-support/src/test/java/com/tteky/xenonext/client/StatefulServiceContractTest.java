package com.tteky.xenonext.client;

import com.vmware.xenon.common.BasicReusableHostTestCase;
import com.vmware.xenon.services.common.ExampleService;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.tteky.xenonext.client.ServiceClientUtil.newStatefulSvcContract;
import static org.junit.Assert.*;

/**
 * Created by mageshwaranr on 17-Dec-16.
 */
public class StatefulServiceContractTest extends BasicReusableHostTestCase {

    @ClassRule
    public static TemporaryFolder tmpFolder = new TemporaryFolder();

    static StatefulServiceContract<ExampleService.ExampleServiceState> testSvc;

    @Before
    public void initializeHost() throws Throwable {
        host.startFactory(new ExampleService());
        CountDownLatch latch = new CountDownLatch(1);
        host.registerForServiceAvailability(((completedOp, failure) -> latch.countDown()), ExampleService.FACTORY_LINK);
        latch.await(500, TimeUnit.MILLISECONDS);
        testSvc = newStatefulSvcContract(host, ExampleService.FACTORY_LINK,
                ExampleService.ExampleServiceState.class);
    }

    @Test
    public void testContract() throws Throwable {
//    initializeHost();
        ExampleService.ExampleServiceState state = new ExampleService.ExampleServiceState();
        state.name = "Example name";
        state.required = "Some mandatory value";
        ExampleService.ExampleServiceState exampleServiceState = testSvc.post(state).get();
        assertEquals(state.name, exampleServiceState.name);
        assertEquals(state.required, exampleServiceState.required);
        String id = ServiceClientUtil.selfLinkToId(exampleServiceState.documentSelfLink);

        exampleServiceState = testSvc.get(id).get();
        assertEquals(state.name, exampleServiceState.name);
        assertEquals(state.required, exampleServiceState.required);

        exampleServiceState = testSvc.getBySelfLink(exampleServiceState.documentSelfLink).get();
        assertEquals(state.name, exampleServiceState.name);
        assertEquals(state.required, exampleServiceState.required);

        state.keyValues = new HashMap<>();
        state.keyValues.put("One", "1");
        exampleServiceState = testSvc.patch(id, state).get();
        assertEquals(state.name, exampleServiceState.name);
        assertEquals(1, exampleServiceState.keyValues.size());

        exampleServiceState = testSvc.delete(id, state).get();
        assertEquals(state.name, exampleServiceState.name);

        try {
            testSvc.get(id).get();
            fail("Shouldn't reach here");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }


}