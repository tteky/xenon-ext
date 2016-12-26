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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * Created by mageshwaranr on 8/22/2016.
 */
public class JaxRsServiceClientTest {

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(8089);


    private static SyncServiceClient clientService;

    @BeforeClass
    public static void initJaxRsClientInvoker() {
        clientService = JaxRsServiceClient.newBuilder()
                .withBaseUri("http://localhost:" + wireMockRule.port())
                .withResourceInterface(SyncServiceClient.class)
                .withGenericTypeResolution("T", SuccessResponse.class)
                .withResponseDecoder(new ProxyHandler()::operationDecoder)
                .build();
        WireMock.reset();
    }

    @Test
    public void testGetAction() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        Map<String, String> action = clientService.getAction("queryValue1", "pathValue1");
        assertNotNull(action);
        assertEquals("success", action.get("result"));
    }


    @Test(expected = Exception.class)
    public void testGetActionWithReturnTypeException() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withBody("string body instead of map")));

        clientService.getAction("queryValue1", "pathValue1");
        fail("Shouldn't reach here");
    }


    @Test
    public void testGetActionWithNoBody() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()));

        Map<String, String> action = clientService.getAction("queryValue1", "pathValue1");
        assertNotNull(action);
        assertTrue(action.isEmpty());
    }


    @Test(expected = Exception.class)
    public void testGetActionWithXenonErrorResponse() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withBody(xenonServiceErrorResp())
                        .withStatus(500)));

        clientService.getAction("queryValue1", "pathValue1");
        fail("Shouldn't reach here");
    }


    @Test(expected = Exception.class)
    public void testGetActionWithServiceException() {
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/pathValue1?query=queryValue1"))
                .willReturn(aResponse()
                        .withStatus(404)));

        clientService.getAction("queryValue1", "pathValue1");
        fail("Shouldn't reach here");
    }

    @Test
    public void testPostAction() {
        List<String> contents = asList("POST_BODY_1", "POST_BODY_2");
        stubFor(post(urlEqualTo("/vrbc/xenon/util/test/post"))
                .withRequestBody(equalToJson(new Gson().toJson(contents)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        Map<String, String> action = clientService.postAction(new ArrayList<>(contents));
        assertNotNull(action);
        assertEquals("success", action.get("result"));
    }


    @Test(expected = Exception.class)
    public void testPostActionFailure() {
        List<String> contents = asList("POST_BODY_1", "POST_BODY_2");
        stubFor(post(urlEqualTo("/vrbc/xenon/util/test/post"))
                .withRequestBody(equalToJson(new Gson().toJson(contents)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        Map<String, String> action = clientService.postAction(new ArrayList<>());
        assertNotNull(action);
        assertEquals("success", action.get("result"));
    }


    @Test
    public void testPatchAction() throws Exception {
        List<Integer> contents = asList(1, 2, 3);
        stubFor(patch(urlEqualTo("/vrbc/xenon/util/test/patch"))
                .withRequestBody(equalToJson(new Gson().toJson(contents)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResp())));

        Map<String, String> action = clientService.patchAction(new ArrayList<>(contents));
        assertNotNull(action);
        assertEquals("success", action.get("result"));
    }

    @Test
    public void testDeleteAction() throws Exception {
        stubFor(delete(urlEqualTo("/vrbc/xenon/util/test/delete/delete_path_param_1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[ \"success\" ] ")));

        List<String> action = clientService.deleteAction("delete_path_param_1");
        assertNotNull(action);
        assertEquals("success", action.get(0));

    }

    @Test
    public void testPostActionWithAuthInfo() {
        List<Integer> contents = asList(1, 2, 3);
        stubFor(post(urlEqualTo("/vrbc/xenon/util/test/post/auth"))
                .withRequestBody(equalToJson(new Gson().toJson(contents)))
                .withCookie("cookie", equalTo("auth-cookie-value"))
                .withHeader("header", equalTo("auth-header-value"))
                .willReturn(aResponse()
                        .withBody("[ \"success\" ] ")));

        List<String> action = clientService.postActionWithAuthInfo("auth-header-value",
                "auth-cookie-value", new ArrayList<>(contents));
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

        Map<String, SuccessResponse> genericReturn = clientService.getActionWithGenericReturn();
        assertNotNull(genericReturn);
        assertEquals(1, genericReturn.size());
        assertTrue(genericReturn.containsKey("result"));
        assertEquals("success", genericReturn.get("result").getMessage());
        assertEquals(20, genericReturn.get("result").getCode());
    }

    @Test
    public void testAppendDefault() {
        assertEquals("Default_value", clientService.appendDefault("value"));
    }

    @Test
    public void testGenericReturnType() {
        SuccessResponse response = new SuccessResponse();
        response.setCode(2);
        response.setMessage("SomeMessage");
        stubFor(get(urlEqualTo("/vrbc/xenon/util/test/get/generic/type"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(new Gson().toJson(response))));

        SuccessResponse actual = clientService.genericReturnType();
        assertEquals(response.getCode(), actual.getCode());
        assertEquals(response.getMessage(), actual.getMessage());
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