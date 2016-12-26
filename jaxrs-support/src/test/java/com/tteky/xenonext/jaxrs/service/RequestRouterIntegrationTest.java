package com.tteky.xenonext.jaxrs.service;

import com.tteky.xenonext.jaxrs.EmployeePojo;
import com.tteky.xenonext.jaxrs.annotation.OperationBody;
import com.tteky.xenonext.jaxrs.annotation.PATCH;
import com.tteky.xenonext.jaxrs.client.JaxRsServiceClient;
import com.tteky.xenonext.util.HttpError;
import com.vmware.xenon.common.BasicReusableHostTestCase;
import com.vmware.xenon.common.ServiceHost;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.vmware.xenon.common.Utils.toJson;
import static org.junit.Assert.*;

/**
 * Created by mageshwaranr on 9/1/2016.
 */
public class RequestRouterIntegrationTest extends BasicReusableHostTestCase {

    private FullSampleService sampleService;

    @Before
    public void init() throws Throwable {
        sampleService = JaxRsServiceClient.newBuilder()
                .withHost(host)
                .withResourceInterface(FullSampleService.class)
                .build();
        try {
            host.startServiceAndWait(SampleServiceImpl.class, SampleService.SELF_LINK);
        } catch (ServiceHost.ServiceAlreadyStartedException throwable) {
            // ignore and proceed.
        }
    }


    @Test
    public void testSimpleGet() throws Throwable {
        Map<String, String> body = sampleService.simpleGet();
        assertEquals("success", body.get("result"));
    }

    @Test
    public void testSimplePatch() throws Throwable {
        Map<String, String> body = sampleService.simplePatch(new String[]{"Some payload"});
        assertEquals("success", body.get("result"));
    }

    @Test
    public void testPostWithQueryAndPath() throws Throwable {
        List<String> payload = Arrays.asList("one", "two");
        Map<String, String> response = sampleService.postWithQueryAndPath("pVal", "qVal", payload);
        assertEquals("success", response.get("result"));
        assertEquals("pVal", response.get("pathParam"));
        assertEquals("qVal", response.get("queryParam"));
        assertEquals(toJson(payload), response.get("body"));
    }

    @Test
    public void testAsyncGetWithQueryAndPath() throws Throwable {
        Map<String, String> response = sampleService.asyncGetWithQueryAndPath("pVal", "qVal").get();
        assertEquals("success", response.get("result"));
        assertEquals("pVal", response.get("pathParam"));
        assertEquals("qVal", response.get("queryParam"));
    }

    @Test
    public void testGetWithQueryAndPathAndReturn() throws Throwable {
        Map<String, String> response = sampleService.getWithQueryAndPathAndReturn("pVal", "qVal");
        assertEquals("success", response.get("result"));
        assertEquals("pVal", response.get("pathParam"));
        assertEquals("qVal", response.get("queryParam"));
    }

    @Test
    public void testPutWithQueryAndPathAndReturn() throws Throwable {
        List<String> payload = Arrays.asList("one", "two");
        Map<String, String> response = sampleService.putWithQueryAndPathAndReturn("pVal", "qVal", payload).get();
        assertEquals("success", response.get("result"));
        assertEquals("pVal", response.get("pathParam"));
        assertEquals("qVal", response.get("queryParam"));
        assertEquals(toJson(payload), response.get("body"));
    }

    @Test
    public void testPostWithModelValidationOnBody() throws Throwable {
        EmployeePojo pojo = new EmployeePojo();
        pojo.setName("Ram");
        pojo.setAge(17); // this should fail
        try {
            sampleService.postWithModelValidationOnBody("pVal", "qVal", pojo);
            fail("Shouldn't reach here");
        } catch (Throwable e) {
            Throwable cause = e.getCause();
            while (cause != null) {
                e = cause;
                cause = e.getCause();
            }
            assertTrue(e instanceof HttpError);
            assertFalse(((HttpError) e).getContext().isEmpty());
        }
        pojo.setAge(20);
        Map<String, String> response = sampleService.postWithModelValidationOnBody("pVal", "qVal", pojo);
        assertEquals("success", response.get("result"));
        assertEquals("pVal", response.get("pathParam"));
        assertEquals("qVal", response.get("queryParam"));
    }

    @Path(SampleServiceImpl.SELF_LINK)
    public interface FullSampleService extends SampleService {

        @Path("/simple")
        @GET
        Map<String, String> simpleGet();

        @Path("/simple")
        @PATCH
        Map<String, String> simplePatch(@OperationBody String[] unused);

        @Path("/path/{pathParam}/query")
        @POST
        Map<String, String> postWithQueryAndPath(final @PathParam("pathParam") String pathValue,
                                                 final @QueryParam("queryParam") String query,
                                                 final @OperationBody List<String> payload);
    }
}
