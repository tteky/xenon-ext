package com.tteky.xenonext.jaxrs.client;

import com.vmware.xenon.common.Operation;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

/**
 * Created by mages_000 on 26-Dec-16.
 */
@RunWith(MockitoJUnitRunner.class)
public class InterceptorChainTest {

    @Mock
    private OperationInterceptor interceptor1, interceptor2;

    @Mock
    private Operation req;

    @Mock
    private Pair<Operation, Throwable> resp;

    private InterceptorChain interceptorChain;

    @Before
    public void init() {
        interceptorChain = new InterceptorChain(interceptor1, interceptor2);
    }

    @Test
    public void interceptBeforeComplete() throws Exception {
        when(interceptor1.interceptBeforeComplete(req)).thenReturn(req);
        when(interceptor2.interceptBeforeComplete(req)).thenReturn(req);

        interceptorChain.interceptBeforeComplete(req);
        InOrder inOrder = inOrder(interceptor1, interceptor2);
        inOrder.verify(interceptor1).interceptBeforeComplete(req);
        inOrder.verify(interceptor2).interceptBeforeComplete(req);

    }

    @Test
    public void interceptAfterComplete() throws Exception {
        when(interceptor1.interceptAfterComplete(req, resp)).thenReturn(resp);
        when(interceptor2.interceptAfterComplete(req, resp)).thenReturn(resp);

        interceptorChain.interceptAfterComplete(req, resp);

        InOrder inOrder = inOrder(interceptor1, interceptor2);

        inOrder.verify(interceptor1).interceptAfterComplete(req, resp);
        inOrder.verify(interceptor2).interceptAfterComplete(req, resp);
    }

}