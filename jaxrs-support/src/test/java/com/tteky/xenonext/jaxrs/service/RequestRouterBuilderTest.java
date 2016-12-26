package com.tteky.xenonext.jaxrs.service;

import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.Service;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.function.Predicate;

import static com.vmware.xenon.common.Service.ServiceOption.URI_NAMESPACE_OWNER;
import static com.vmware.xenon.common.UriUtils.buildUriPath;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Created by mageshwaranr on 8/12/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class RequestRouterBuilderTest {

    @Mock
    private Operation op;

    @Mock
    private Service service;


    @Test
    public void testNewURIMatcherWithOutPathParam() throws Exception {
        String path = buildUriPath("/vrbc/parent/", "/child/");
        Predicate<Operation> matcher = RequestRouterBuilder.newUriMatcher(path);
        when(op.getUri())
                .thenReturn(new URI("/vrbc/parent/child"))
                .thenReturn(new URI("/vrbc/parent/childs"));
        assertTrue(matcher.test(op));
        assertFalse(matcher.test(op));

        path = buildUriPath("/vrbc/parent/1/", "/child/");
        matcher = RequestRouterBuilder.newUriMatcher(path);
        when(op.getUri()).thenReturn(new URI("/vrbc/parent/child"));
        assertFalse(matcher.test(op));
    }


    @Test
    public void testNewURIMatcherWithPathParam() throws Exception {
        String path = buildUriPath("/vrbc/parent/", "/child/{pathParam}");
        Predicate<Operation> matcher = RequestRouterBuilder.newUriMatcher(path);
        when(op.getUri())
                .thenReturn(new URI("/vrbc/parent/child/pathParamValue"))
                .thenReturn(new URI("/vrbc/parent/child/pathParamValue/2"))
                .thenReturn(new URI("/vrbc/parent/1/child/pathParamValue"));
        assertTrue(matcher.test(op));
        assertFalse(matcher.test(op));
        assertFalse(matcher.test(op));

        path = buildUriPath("/vrbc/parent/", "/{firstPathParam}/child/{secondPathParam}");
        matcher = RequestRouterBuilder.newUriMatcher(path);
        when(op.getUri())
                .thenReturn(new URI("/vrbc/parent/param1/child/param2"))
                .thenReturn(new URI("/vrbc/parent/child/child/child"))
                .thenReturn(new URI("/vrbc/parent/child/child/child/child"));
        assertTrue(matcher.test(op));
        assertTrue(matcher.test(op));
        assertFalse(matcher.test(op));
    }


    @Test(expected = IllegalArgumentException.class)
    public void buildJaxRsRouterShouldFailWhenNamespaceOwnerOperationsIsNotSet() throws Exception {
        when(service.hasOption(URI_NAMESPACE_OWNER)).thenReturn(false);
        RequestRouterBuilder.parseJaxRsAnnotations(service);
    }


}