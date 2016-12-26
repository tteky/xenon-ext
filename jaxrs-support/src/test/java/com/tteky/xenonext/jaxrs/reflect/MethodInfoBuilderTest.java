package com.tteky.xenonext.jaxrs.reflect;

import com.tteky.xenonext.jaxrs.SuccessResponse;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.Service;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tteky.xenonext.jaxrs.reflect.ParamMetadata.Type.*;
import static com.vmware.xenon.common.UriUtils.buildUriPath;
import static org.junit.Assert.*;

/**
 * Created by mageshwaranr on 8/18/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class MethodInfoBuilderTest {

    private Map<String, Class<?>> typeResolution;

    @Before
    public void init() {
        typeResolution = new HashMap<>();
        typeResolution.put("S1", String.class);
        typeResolution.put("S2", Long.class);
    }

    @Test
    public void testParseDeleteApi() throws Exception {
        List<MethodInfo> httpMethods = MethodInfoBuilder.parseInterfaceForJaxRsInfo(MockWithJaxRsParams.class, Collections.emptyMap());
        assertEquals(4, httpMethods.size());

        MethodInfo testDeleteAction = httpMethods.stream().filter(method -> method.getName().equals("testDeleteAction")).findFirst().get();
        assertEquals(1, testDeleteAction.getParameters().size());
        assertEquals("path", testDeleteAction.getParameters().get(0).getName());
        assertEquals(PATH, testDeleteAction.getParameters().get(0).getType());
        assertEquals("/delete/{path}", testDeleteAction.getUriPath());
        assertEquals(Service.Action.DELETE, testDeleteAction.getAction());
        assertFalse(testDeleteAction.getPathParamsVsUriIndex().isEmpty());
        assertTrue(testDeleteAction.getPathParamsVsUriIndex().containsKey("path"));
    }

    @Test
    public void testParsePatchApi() throws Exception {
        List<MethodInfo> httpMethods = MethodInfoBuilder.parseInterfaceForJaxRsInfo(MockWithJaxRsParams.class, typeResolution);
        assertEquals(4, httpMethods.size());

        MethodInfo testPatchAction = httpMethods.stream().filter(method -> method.getName().equals("testPatchAction")).findFirst().get();
        assertEquals(3, testPatchAction.getParameters().size());
        assertEquals("path", testPatchAction.getParameters().get(0).getName());
        assertEquals(PATH, testPatchAction.getParameters().get(0).getType());
        assertEquals(BODY, testPatchAction.getParameters().get(1).getType());
        assertEquals(OPERATION, testPatchAction.getParameters().get(2).getType());
        assertEquals("/patch/{path}", testPatchAction.getUriPath());
        assertEquals(Service.Action.PATCH, testPatchAction.getAction());
        assertFalse(testPatchAction.getPathParamsVsUriIndex().isEmpty());
        assertTrue(testPatchAction.getPathParamsVsUriIndex().containsKey("path"));
    }

    @Test
    public void testParseGetApiWithPathAndQueryParam() throws Exception {
        List<MethodInfo> httpMethods = MethodInfoBuilder.parseInterfaceForJaxRsInfo(MockWithJaxRsParams.class, typeResolution);
        assertEquals(4, httpMethods.size());

        MethodInfo testGetAction = httpMethods.stream().filter(method -> method.getName().equals("testGetAction")).findFirst().get();
        assertEquals(3, testGetAction.getParameters().size());
        assertEquals("path", testGetAction.getParameters().get(1).getName());
        assertEquals(PATH, testGetAction.getParameters().get(1).getType());
        assertEquals("query", testGetAction.getParameters().get(0).getName());
        assertEquals(QUERY, testGetAction.getParameters().get(0).getType());
        assertEquals(OPERATION, testGetAction.getParameters().get(2).getType());
        assertEquals(Service.Action.GET, testGetAction.getAction());
        assertTrue(testGetAction.getPathParamsVsUriIndex().isEmpty());
    }

    @Test
    public void testAyncApi() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getMethod("testPostAction", String.class, List.class);
        List<MethodInfo> infos = MethodInfoBuilder.generateMethodInfo(new Method[]{publicMethod}, typeResolution);
        assertEquals(1, infos.size());
        assertTrue(infos.get(0).isAsyncApi());
        assertEquals(List.class, ((ParameterizedType) infos.get(0).getType()).getRawType());
        assertEquals(List.class, infos.get(0).getReturnType());
    }

    @Test
    public void testParseAction() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getMethod("testGetAction", String.class, String.class, Operation.class);
        Service.Action action = MethodInfoBuilder.parseAction(publicMethod);
        assertEquals(Service.Action.GET, action);

        publicMethod = MockWithJaxRsParams.class.getMethod("testPatchAction", String.class, List.class, Operation.class);
        action = MethodInfoBuilder.parseAction(publicMethod);
        assertEquals(Service.Action.PATCH, action);

        publicMethod = MockWithJaxRsParams.class.getMethod("testDeleteAction", String.class);
        action = MethodInfoBuilder.parseAction(publicMethod);
        assertEquals(Service.Action.DELETE, action);
    }

    @Test
    public void testParsePathParams() throws Exception {
        Map<String, Integer> pathParams = MethodInfoBuilder.parsePathParams(buildUriPath("/vrbc/parent/", "/child/{pathParam}"));
        assertEquals(1, pathParams.size());
        assertEquals(Integer.valueOf(4), pathParams.get("pathParam"));

        pathParams = MethodInfoBuilder.parsePathParams(buildUriPath("/vrbc/parent/", "/child/{pathParam1}/{pathParam2}"));
        assertEquals(2, pathParams.size());
        assertEquals(Integer.valueOf(4), pathParams.get("pathParam1"));
        assertEquals(Integer.valueOf(5), pathParams.get("pathParam2"));
    }

    @Test
    public void testExtractParams() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("methodWithQueryAndPathParam", String.class, String.class);
        List<ParamMetadata> params = MethodInfoBuilder.extractParamMetadatas(publicMethod);
        assertEquals(2, params.size());
        Collections.sort(params);
        assertEquals("query", params.get(0).getName());
        assertEquals(QUERY, params.get(0).getType());
        assertEquals("path", params.get(1).getName());
        assertEquals(PATH, params.get(1).getType());
    }

    @Test
    public void testExtractParamsForMethodWithQueryAndOperation() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getMethod("methodWithQueryAndOperation", String.class, Operation.class);
        List<ParamMetadata> params = MethodInfoBuilder.extractParamMetadatas(publicMethod);
        assertEquals(2, params.size());
        assertEquals(1, params.get(1).getParameterIndex());
        assertEquals(OPERATION, params.get(1).getType());
    }

    @Test
    public void testExtractParamsForMethodWithCookieAndHeader() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getMethod("methodWithCookieAndHeader", String.class, String.class);
        List<ParamMetadata> params = MethodInfoBuilder.extractParamMetadatas(publicMethod);
        assertEquals(2, params.size());
        ParamMetadata paramMetadata = params.get(1);
        assertEquals(1, paramMetadata.getParameterIndex());
        assertEquals(HEADER, paramMetadata.getType());
        assertEquals("header", paramMetadata.getName());
        paramMetadata = params.get(0);
        assertEquals(0, paramMetadata.getParameterIndex());
        assertEquals(COOKIE, paramMetadata.getType());
        assertEquals("cookie", paramMetadata.getName());
    }


    @Test
    public void testExtractParamsForMethodWithPathParamAndBody() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getMethod("methodWithPathParamAndBody", String.class, Object.class);
        List<ParamMetadata> params = MethodInfoBuilder.extractParamMetadatas(publicMethod);
        assertEquals(2, params.size());
        assertEquals(1, params.get(1).getParameterIndex());
        assertEquals(BODY, params.get(1).getType());
        assertEquals(Object.class, params.get(1).getParamterType());
    }

    @Test
    public void testExtractParamsForMethodWithPathParamOnly() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getMethod("methodWithPathParamOnly", String.class);
        List<ParamMetadata> params = MethodInfoBuilder.extractParamMetadatas(publicMethod);
        assertEquals(1, params.size());
        assertEquals("path", params.get(0).getName());
        assertEquals(PATH, params.get(0).getType());
    }

    @Test
    public void testExtractParamsFOrMethodWithQueryParamOnly() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getMethod("methodWithQueryParamOnly", String.class);
        List<ParamMetadata> params = MethodInfoBuilder.extractParamMetadatas(publicMethod);
        assertEquals(1, params.size());
        assertEquals("query", params.get(0).getName());
        assertEquals(QUERY, params.get(0).getType());
    }

    @Test
    public void testExtractParamsOnMethodWithQueryAndPathParams() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getMethod("methodWithQueryAndPathParams", String.class, String.class, String.class, String.class);
        List<ParamMetadata> params = MethodInfoBuilder.extractParamMetadatas(publicMethod);
        assertEquals(4, params.size());
        Collections.sort(params);
        assertEquals("query1", params.get(0).getName());
        assertEquals(QUERY, params.get(0).getType());
        assertEquals("query2", params.get(1).getName());
        assertEquals(QUERY, params.get(1).getType());
        assertEquals("path1", params.get(2).getName());
        assertEquals(PATH, params.get(2).getType());
        assertEquals("path2", params.get(3).getName());
        assertEquals(PATH, params.get(3).getType());
    }


    @Test(expected = IllegalArgumentException.class)
    public void testExtractParamsForMethodWithUnsupportedAnnotation() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("methodWithUnsupportedAnnotation", String.class, Operation.class);
        MethodInfoBuilder.extractParamMetadatas(publicMethod);
        fail("Shouldn't reach here");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractParamsForMethodWithUnannotatedArgAndIncorrectType() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("methodWithUnannotatedArgAndIncorrectType", String.class, Object.class);
        MethodInfoBuilder.extractParamMetadatas(publicMethod);
        fail("Shouldn't reach here");

    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractParamsForMethodWithUnannotatedArgs() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("methodWithUnannotatedArgs", String.class,
                Operation.class, Object.class);
        MethodInfoBuilder.extractParamMetadatas(publicMethod);
        fail("Shouldn't reach here");
    }

    @Test
    public void testMethodWithGenericReturnType() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("methodWithGenericReturnType");
        MethodInfo mInfo = new MethodInfo(publicMethod);
        MethodInfoBuilder.parseReturnTypes(mInfo, typeResolution);
        assertEquals(Map.class, mInfo.getReturnType());
        assertEquals(Map.class, ((ParameterizedType) mInfo.getType()).getRawType());

        publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("methodWithReturnType");
        mInfo = new MethodInfo(publicMethod);
        MethodInfoBuilder.parseReturnTypes(mInfo, typeResolution);
        assertEquals(SuccessResponse.class, mInfo.getReturnType());

        publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("methodWithGenericDynamicReturnType");
        mInfo = new MethodInfo(publicMethod);
        MethodInfoBuilder.parseReturnTypes(mInfo, typeResolution);
        assertEquals(String.class, mInfo.getReturnType());

    }

    @Test
    public void testAsyncMethodReturnType() throws Exception {
        Method publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("asyncMethodWithGenericReturnType");
        MethodInfo mInfo = new MethodInfo(publicMethod);
        MethodInfoBuilder.parseReturnTypes(mInfo, typeResolution);
        assertEquals(Map.class, mInfo.getReturnType());
        assertEquals(Map.class, ((ParameterizedType) mInfo.getType()).getRawType());

        publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("asyncMethodWithWildCardReturnType");
        mInfo = new MethodInfo(publicMethod);
        MethodInfoBuilder.parseReturnTypes(mInfo, typeResolution);
        assertEquals(Object.class, mInfo.getReturnType());

        publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("asyncMethodWithArrayReturnType");
        mInfo = new MethodInfo(publicMethod);
        MethodInfoBuilder.parseReturnTypes(mInfo, typeResolution);
        assertEquals(String[].class, mInfo.getReturnType());

        publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("asyncMethodWithNonGenericReturnType");
        mInfo = new MethodInfo(publicMethod);
        MethodInfoBuilder.parseReturnTypes(mInfo, typeResolution);
        assertEquals(Object.class, mInfo.getReturnType());

        publicMethod = MockWithJaxRsParams.class.getDeclaredMethod("asyncMethodWithGenericDynamicReturnType");
        mInfo = new MethodInfo(publicMethod);
        MethodInfoBuilder.parseReturnTypes(mInfo, typeResolution);
        assertEquals(Long.class, mInfo.getReturnType());
    }

}