package com.tteky.xenonext.jaxrs.service;

import com.tteky.xenonext.jaxrs.annotation.OperationBody;
import com.tteky.xenonext.jaxrs.annotation.PATCH;
import com.tteky.xenonext.jaxrs.reflect.MethodInfo;
import com.tteky.xenonext.jaxrs.reflect.MethodInfoBuilder;
import com.tteky.xenonext.util.HttpError;
import com.tteky.xenonext.util.HttpErrorResponse;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.Service;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by mageshwaranr on 8/17/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class RoutingOperationHandlerTest {

    @Mock
    private Operation op;

    @Mock
    private DecoratedMockService service;

    @Mock
    private List<String> mockedBody;

    @Test
    public void testOperationHandlerForGetAndPostParam() throws Exception {
        //given
        Method getMethod = DecoratedMockService.class.getMethod("testGetAction", String.class, String.class, Operation.class);

        List<MethodInfo> methodInfos = MethodInfoBuilder.generateMethodInfo(new Method[]{getMethod}, Collections.emptyMap());

        String path = "/resource/get/{path}";
        RoutingOperationHandler testClass = new RoutingOperationHandler(path, methodInfos.get(0), service);
        testClass.init();
        when(op.getUri()).thenReturn(new URI("/resource/get/PathValue1?query=QueryValue1"));
        //test
        testClass.accept(op);
        //verify
        verify(service).testGetAction("QueryValue1", "PathValue1", op);
        assertFalse(testClass.hasValidReturnType);
        assertTrue(testClass.hasOperationAsAnArgument);
    }

    @Test
    public void testOperationHandlerForPatchWithBodyParam() throws Exception {
        //given
        Method patchMethod = DecoratedMockService.class.getMethod("testPatchAction", String.class, List.class, Operation.class);
        String path = "/resource/patch/{path}";

        List<MethodInfo> methodInfos = MethodInfoBuilder.generateMethodInfo(new Method[]{patchMethod}, Collections.emptyMap());
        RoutingOperationHandler testClass = new RoutingOperationHandler(path, methodInfos.get(0), service);

        testClass.init();
        when(op.getUri()).thenReturn(new URI("/resource/patch/PathValue1/PathValue2"));
        when(op.getBody(List.class)).thenReturn(mockedBody);
        //test
        testClass.accept(op);
        //verify
        verify(service).testPatchAction("PathValue1", mockedBody, op);
        assertFalse(testClass.hasValidReturnType);
        assertTrue(testClass.hasOperationAsAnArgument);
    }

    @Test
    public void testOperationHandlerForDeleteWithOutOperation() throws Exception {
        //given
        Method deleteMethod = DecoratedMockService.class.getMethod("testDeleteAction",
                String.class, String.class, String.class);
        String path = "/resource/delete/{path}";

        List<MethodInfo> methodInfos = MethodInfoBuilder.generateMethodInfo(new Method[]{deleteMethod}, Collections.emptyMap());


        RoutingOperationHandler testClass = new RoutingOperationHandler(path, methodInfos.get(0), service);
        testClass.init();
        when(op.getUri()).thenReturn(new URI("/resource/delete/PathValue1/PathValue2"));
        when(op.getRequestHeader("header")).thenReturn("headerValue");
        when(service.testDeleteAction("PathValue1", "headerValue", null)).thenReturn(mockedBody);
        //test
        testClass.accept(op);
        //verify
        verify(service).testDeleteAction("PathValue1", "headerValue", null);
        assertTrue(testClass.hasValidReturnType);
        assertFalse(testClass.hasOperationAsAnArgument);
        verify(op).setBody(mockedBody);
        verify(op).complete();
    }


    @Test
    public void testOperationHandlerForPost() throws Exception {
        //given
        Method postMethod = DecoratedMockService.class.getMethod("testPostAction", String.class, List.class);
        String path = "/resource/post/{path}";

        List<MethodInfo> methodInfos = MethodInfoBuilder.generateMethodInfo(new Method[]{postMethod}, Collections.emptyMap());

        RoutingOperationHandler testClass = new RoutingOperationHandler(path, methodInfos.get(0), service);
        testClass.init();
        when(op.getUri()).thenReturn(new URI("/resource/post/PathValue1"));
        CompletableFuture<List<String>> successFuture = new CompletableFuture<>();
        when(service.testPostAction("PathValue1", mockedBody))
                .thenReturn(successFuture);
        when(op.getBody(List.class)).thenReturn(mockedBody);
        //test
        testClass.accept(op);
        List<String> result = asList("1", "2");
        successFuture.complete(result);

        //verify
        verify(service).testPostAction("PathValue1", mockedBody);
        assertTrue(testClass.hasValidReturnType);
        assertFalse(testClass.hasOperationAsAnArgument);
        verify(op).setBody(result);
        verify(op).complete();
    }

    @Test
    public void testOperationHandlerForPostWithExceptionFlow() throws Exception {
        //given
        Method postMethod = DecoratedMockService.class.getMethod("testPostAction", String.class, List.class);
        String path = "/resource/post/{path}";

        List<MethodInfo> methodInfos = MethodInfoBuilder.generateMethodInfo(new Method[]{postMethod}, Collections.emptyMap());

        RoutingOperationHandler testClass = new RoutingOperationHandler(path, methodInfos.get(0), service);
        testClass.init();
        when(op.getUri()).thenReturn(new URI("/resource/post/PathValue1"));
        CompletableFuture<List<String>> failureFuture1 = new CompletableFuture<>();
        CompletableFuture<List<String>> failureFuture2 = new CompletableFuture<>();
        when(service.testPostAction("PathValue1", mockedBody))
                .thenReturn(failureFuture1)
                .thenReturn(failureFuture2);
        when(op.getBody(List.class)).thenReturn(mockedBody);
        //test
        testClass.accept(op);
        failureFuture1.completeExceptionally(new HttpError(100));

        //verify
        verify(service).testPostAction("PathValue1", mockedBody);
        assertTrue(testClass.hasValidReturnType);
        assertFalse(testClass.hasOperationAsAnArgument);
        verify(op).fail(any(HttpError.class), any(HttpErrorResponse.class));

        //setup
        failureFuture2.completeExceptionally(new CompletionException(new NullPointerException()));
        //test
        testClass.accept(op);
        //verify
        verify(service, times(2)).testPostAction("PathValue1", mockedBody);
        assertTrue(testClass.hasValidReturnType);
        assertFalse(testClass.hasOperationAsAnArgument);
        verify(op, times(2)).fail(any(NullPointerException.class), any(HttpErrorResponse.class));
    }

    @Test
    public void testOperationHandlerForPostWithExceptionFlow2() throws Exception {
        //given
        Method postMethod = DecoratedMockService.class.getMethod("testPostAction", String.class, List.class);
        String path = "/resource/post/{path}";

        List<MethodInfo> methodInfos = MethodInfoBuilder.generateMethodInfo(new Method[]{postMethod}, Collections.emptyMap());

        RoutingOperationHandler testClass = new RoutingOperationHandler(path, methodInfos.get(0), service);
        testClass.init();
        when(op.getUri()).thenReturn(new URI("/resource/post/PathValue1"));

        when(service.testPostAction("PathValue1", mockedBody))
                .thenThrow(new NullPointerException());
        when(op.getBody(List.class)).thenReturn(mockedBody);

        //test
        testClass.accept(op);

        //verify
        verify(service).testPostAction("PathValue1", mockedBody);
        assertTrue(testClass.hasValidReturnType);
        assertFalse(testClass.hasOperationAsAnArgument);
        verify(op).fail(any(NullPointerException.class), any(String.class));

    }


    interface DecoratedMockService extends Service {

        @GET
        void testGetAction(@QueryParam("query") String query, @PathParam("path") String path, Operation op);


        @PATCH
        @Path("/patch/{path}")
        void testPatchAction(@PathParam("path") String path, @OperationBody List<String> contents, Operation op);

        @DELETE
        @Path("/delete/{path}")
        List<String> testDeleteAction(@PathParam("path") String path, @HeaderParam("header") String header,
                                      @CookieParam("cookie") String cookie);

        @POST
        @Path("/post/{path}")
        CompletableFuture<List<String>> testPostAction(@PathParam("path") String path,
                                                       @OperationBody List<String> contents);

    }

}