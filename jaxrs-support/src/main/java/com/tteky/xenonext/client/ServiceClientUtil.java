package com.tteky.xenonext.client;

import com.vmware.xenon.common.ServiceHost;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by mageshwaranr
 */
public class ServiceClientUtil {

    public static int randomAvailablePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    public static void waitForServiceAvailability(ServiceHost host, String... paths) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        host.registerForServiceAvailability(((completedOp, failure) -> latch.countDown()), paths);
        latch.await(5, TimeUnit.SECONDS);
    }


}
