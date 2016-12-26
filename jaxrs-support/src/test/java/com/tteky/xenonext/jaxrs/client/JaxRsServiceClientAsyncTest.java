package com.tteky.xenonext.jaxrs.client;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.gson.Gson;
import com.tteky.xenonext.jaxrs.SuccessResponse;
import com.vmware.xenon.common.ServiceErrorResponse;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * Created by mageshwaranr on 9/9/2016.
 */
public class JaxRsServiceClientAsyncTest {

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(8089);


    private static AsyncServiceClient asyncService;

    @BeforeClass
    public static void initJaxRsClientInvoker() {
        asyncService = JaxRsServiceClient.newBuilder()
                .withBaseUri("http://localhost:" + wireMockRule.port())
                .withResourceInterface(AsyncServiceClient.class)
                .build();
        WireMock.reset();
    }


    @Test
    public void testGetActionAsync() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        CompletableFuture<Map<String, String>> future = asyncService.getAction("queryValue1", "pathValue1");
        Map<String, String> action = future.join();
        assertNotNull(action);
        assertEquals("success", action.get("result"));
    }

    @Test
    public void testInvokeGetAction() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        CompletableFuture<Map<String, String>> future = asyncService.invokeGetAction("query", "path");
        Map<String, String> action = future.join();
        assertNotNull(action);
        assertEquals("success", action.get("result"));
    }


    @Test(expected = Exception.class)
    public void testGetActionAsyncWithReturnTypeException() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withBody("string body instead of map")));

        CompletableFuture<Map<String, String>> future = asyncService.getAction("queryValue1", "pathValue1");
        future.join();
        fail("Shouldn't reach here");
    }


    @Test
    public void testGetActionAsyncWithNoBody() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()));

        CompletableFuture<Map<String, String>> future = asyncService.getAction("queryValue1", "pathValue1");
        future.join();
        Map<String, String> action = future.join();
        assertNotNull(action);
        assertTrue(action.isEmpty());
    }


    @Test(expected = CompletionException.class)
    public void testGetActionAsyncWithXenonErrorResponse() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withBody(xenonServiceErrorResp())
                        .withStatus(500)));

        CompletableFuture<Map<String, String>> future = asyncService.getAction("queryValue1", "pathValue1");
        future.join();
        fail("Shouldn't reach here");
    }


    @Test(expected = CompletionException.class)
    public void testGetActionAsyncWithServiceException() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withStatus(500)));

        CompletableFuture<Map<String, String>> future = asyncService.getAction("queryValue1", "pathValue1");
        future.join();
        fail("Shouldn't reach here");
    }


    @Test
    public void testPostActionAsync() {
        List<String> contents = asList("POST_BODY_1", "POST_BODY_2");
        stubFor(post(urlEqualTo("/vrbc/xenon/util/test/post"))
                .withRequestBody(equalToJson(new Gson().toJson(contents)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        CompletableFuture<Void> future = asyncService.postAction(new ArrayList<>(contents));
        future.join();
        assertTrue("Void method works fine", true);
        verify(postRequestedFor(urlEqualTo("/vrbc/xenon/util/test/post"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test(expected = CompletionException.class)
    public void testPostActionAsyncFailure() {
        List<String> contents = asList("POST_BODY_1", "POST_BODY_2");
        stubFor(post(urlEqualTo("/vrbc/xenon/util/test/post"))
                .withRequestBody(equalToJson(new Gson().toJson(contents)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        CompletableFuture<Void> future = asyncService.postAction(new ArrayList<>());
        future.join();
        fail("Shouldn't reach here");
    }


    @Test
    public void testPostActionWithAuthInfoAysnc() {
        List<Integer> contents = asList(1, 2, 3);
        stubFor(post(urlEqualTo("/vrbc/xenon/util/test/post/auth"))
                .withRequestBody(equalToJson(new Gson().toJson(contents)))
                .withCookie("cookie", equalTo("auth-cookie-value"))
                .withHeader("header", equalTo("auth-header-value"))
                .willReturn(aResponse()
                        .withBody("[ \"success\" ] ")));


        List<String> action = asyncService.postActionWithAuthInfo("auth-header-value",
                "auth-cookie-value", new ArrayList<>(contents)).join();
        assertNotNull(action);
        assertEquals("success", action.get(0));
    }


    @Test
    public void testGetActionWithGenericReturn() {
        Map<String, Object> outer = new HashMap<>();
        Map<String, Object> inner = new HashMap<>();
        inner.put("code", 20);
        inner.put("message", "success");
        outer.put("result", inner);
        String successResp = new Gson().toJson(outer);

        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/genericReturn"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("custom-header", "Custom-Value")
                        .withBody(successResp)));

        Map<String, SuccessResponse> genericReturn = asyncService.getActionWithGenericReturn().join();
        assertNotNull(genericReturn);
        assertEquals(1, genericReturn.size());
        assertTrue(genericReturn.containsKey("result"));
        assertEquals("success", genericReturn.get("result").getMessage());
        assertEquals(20, genericReturn.get("result").getCode());
    }


    String successResp() {
        Map<String, String> map = new HashMap<>();
        map.put("result", "success");
        return new Gson().toJson(map);
    }

    String xenonServiceErrorResp() {
        ServiceErrorResponse errorRsp = ServiceErrorResponse.create(new IllegalArgumentException("bad request"), 404);
        return new Gson().toJson(errorRsp);
    }

}
