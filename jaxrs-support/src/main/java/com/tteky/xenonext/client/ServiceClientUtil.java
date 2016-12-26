package com.tteky.xenonext.client;

import com.tteky.xenonext.jaxrs.client.JaxRsServiceClient;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceHost;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.vmware.xenon.common.UriUtils.extendUri;

/**
 * Created by mageshwaranr
 */
public class ServiceClientUtil {

    public static int randomAvailablePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    public static void waitForServiceAvailability(@NotNull ServiceHost host, String... paths) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        host.registerForServiceAvailability(((completedOp, failure) -> latch.countDown()), paths);
        latch.await(5, TimeUnit.SECONDS);
    }

    public static String selfLinkToId(@NotNull String selfLink) {
        return selfLink.substring(selfLink.lastIndexOf("/") + 1, selfLink.length());
    }

    public static <T extends ServiceDocument> StatefulServiceContract<T> newStatefulSvcContract(ServiceHost host, String baseUri, Class<T> clazz) {
        return JaxRsServiceClient.newBuilder()
                .withHost(host)
                .withBaseUri(extendUri(host.getPublicUri(), baseUri))
                .withResourceInterface(StatefulServiceContract.class)
                .withGenericTypeResolution("T", clazz)
                .build();
    }




}
