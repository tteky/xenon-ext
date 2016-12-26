package com.tteky.xenonext.client;

import org.junit.Test;

import static com.tteky.xenonext.client.ServiceClientUtil.randomAvailablePort;
import static com.tteky.xenonext.client.ServiceClientUtil.selfLinkToId;
import static org.junit.Assert.*;

/**
 * Created by mages_000 on 26-Dec-16.
 */
public class ServiceClientUtilTest {
    @Test
    public void testSelfLinkToId() throws Exception {
        String selfLink = "/core/node-groups/id";
        assertEquals("id", selfLinkToId(selfLink));
        selfLink = "/core/node-groups/some-other-id";
        assertEquals("some-other-id", selfLinkToId(selfLink));
        selfLink = "only-id";
        assertEquals("only-id", selfLinkToId(selfLink));
    }

    @Test
    public void testRandomAvailablePort() throws Exception {
        int availablePort = randomAvailablePort();
        assertTrue(availablePort > 0);
        assertNotEquals(availablePort, randomAvailablePort());
    }

}