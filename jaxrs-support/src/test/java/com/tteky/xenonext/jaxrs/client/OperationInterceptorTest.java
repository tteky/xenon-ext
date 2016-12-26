package com.tteky.xenonext.jaxrs.client;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.gson.Gson;
import com.vmware.xenon.common.Operation;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.vmware.xenon.common.UriUtils.buildUri;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by kseshadri
 */
public class OperationInterceptorTest {

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(8089);

    @Test
    public void getWithOperationInterceptorShouldPass() {
        SyncServiceClient clientService = JaxRsServiceClient.newBuilder()
                .withBaseUri("http://localhost:" + wireMockRule.port())
                .withResourceInterface(SyncServiceClient.class)
                .withInterceptor(new ChangeOperationInterceptor())
                .build();

        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue2?query=queryValue2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        Map<String, String> action = clientService.getAction("queryValue1", "pathValue1");
        assertNotNull(action);
        assertEquals("success", action.get("result"));
    }

    @Test(expected = RuntimeException.class)
    public void getWithoutOperationInterceptorShouldFail() {
        SyncServiceClient clientService = JaxRsServiceClient.newBuilder()
                .withBaseUri("http://localhost:" + wireMockRule.port())
                .withResourceInterface(SyncServiceClient.class)
                .build();

        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue2?query=queryValue2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        clientService.getAction("queryValue1", "pathValue1");
    }

    @Test
    public void getWithResultInterceptorShouldPass() {
        SyncServiceClient clientService = JaxRsServiceClient.newBuilder()
                .withBaseUri("http://localhost:" + wireMockRule.port())
                .withResourceInterface(SyncServiceClient.class)
                .withInterceptor(new ChangeResultInterceptor())
                .build();

        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        Map<String, String> action = clientService.getAction("queryValue1", "pathValue1");
        assertNotNull(action);
        assertEquals("failure", action.get("result"));
    }

    @Test
    public void postWithResultInterceptorShouldPass() {
        SyncServiceClient clientService = JaxRsServiceClient.newBuilder()
                .withBaseUri("http://localhost:" + wireMockRule.port())
                .withResourceInterface(SyncServiceClient.class)
                .withInterceptor(new ChangeResultInterceptor())
                .build();

        List<String> contents = asList("POST_BODY_1", "POST_BODY_2");
        stubFor(post(urlEqualTo("/vrbc/xenon/util/test/post"))
                .withRequestBody(equalToJson(new Gson().toJson(contents)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        Map<String, String> action = clientService.postAction(new ArrayList<>(contents));
        assertNotNull(action);
        assertEquals("failure", action.get("result"));
    }


    private String successResp() {
        Map<String, String> map = new HashMap<>();
        map.put("result", "success");
        return new Gson().toJson(map);
    }


    public static class ChangeOperationInterceptor implements OperationInterceptor {

        @Override
        public Operation interceptBeforeComplete(Operation op) {
            op.setUri(buildUri("http://localhost:" + wireMockRule.port() + "/vrbc/xenon/util/test/get/pathValue2?query=queryValue2"));
            return op;
        }

    }

    public static class ChangeResultInterceptor implements OperationInterceptor {

        @Override
        public Pair<Operation, Throwable> interceptAfterComplete(Operation op, Pair<Operation, Throwable> result) {
            result.getLeft().setBody(failureResp());
            return result;
        }

        private String failureResp() {
            Map<String, String> map = new HashMap<>();
            map.put("result", "failure");
            return new Gson().toJson(map);
        }

    }

}
