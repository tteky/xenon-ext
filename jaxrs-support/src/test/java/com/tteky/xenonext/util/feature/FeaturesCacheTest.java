package com.tteky.xenonext.util.feature;

import com.tteky.xenonext.jaxrs.client.JaxRsServiceClient;
import com.vmware.xenon.common.test.VerificationHost;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mageshwaranr on 20-Jan-17.
 */
public class FeaturesCacheTest {


    private static FeatureService service;

    @BeforeClass
    public static void initialize() throws Throwable {
        VerificationHost host = VerificationHost.create(0);
        host.setAuthorizationEnabled(false);
        host.start();
        host.setPeerSynchronizationEnabled(true);
        host.setUpPeerHosts(3);
        host.setNodeGroupQuorum(3);
        host.joinNodesAndVerifyConvergence(3, true);
        List<VerificationHost> values = new ArrayList<>(host.getInProcessHostMap().values());
        for (int i = 0; i < values.size(); i++) {
            setup(values.get(i), i + 1);
        }
    }

    private static void setup(VerificationHost host, int i) throws Throwable {
        host.startFactory(new FeatureDao());
        host.startService(new FeatureServiceImpl());
        host.waitForServiceAvailable(FeatureService.SELF_LINK, FeatureDao.FACTORY_LINK);
        service = JaxRsServiceClient.newBuilder()
                .withHost(host)
                .withResourceInterface(FeatureService.class)
                .build();
        if (i % 2 == 0) {
            FeaturesCache.initialize(host);
        }

    }

    @Test
    public void findFeatureWhenCacheIsNotInitialized() throws Exception {
        Feature feature = new Feature();
        feature.group = "Test";
        feature.name = "notInitialized";
        feature.enable = true;
        Feature fromCache = FeaturesCache.findFeature(feature);
        assertFalse(fromCache.enable);
    }


    @Test
    public void findFeatureEnabled() throws Exception {
        Feature feature = new Feature();
        feature.group = "Test";
        feature.name = "Enabled";
        feature.enable = true;
        service.newFeature(feature).join();

        // cache refreshes async. So loop and wait
        // new feature creation
        Feature fromCache = null;
        for (int i = 0; i < 25; i++) {
            fromCache = FeaturesCache.findFeature(feature);
            if (fromCache.enable)
                break;
            Thread.sleep(100);
        }
        assertTrue(fromCache.enable);

        // disable feature
        service.disableFeature(feature.group, feature.name).join();

        for (int i = 0; i < 25; i++) {
            fromCache = FeaturesCache.findFeature(feature);
            if (!fromCache.enable)
                break;
            Thread.sleep(100);
        }
        assertFalse(fromCache.enable);

        // enable feature again
        service.enableFeature(feature.group, feature.name).join();

        for (int i = 0; i < 25; i++) {
            fromCache = FeaturesCache.findFeature(feature);
            if (fromCache.enable)
                break;
            Thread.sleep(100);
        }
        assertTrue(fromCache.enable);

    }

}