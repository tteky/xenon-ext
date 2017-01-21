package com.tteky.xenonext.util.feature;

import com.tteky.xenonext.jaxrs.client.JaxRsServiceClient;
import com.vmware.xenon.common.BasicReusableHostTestCase;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static com.tteky.xenonext.client.ServiceClientUtil.selfLinkToId;
import static com.tteky.xenonext.util.feature.Feature.formId;
import static org.junit.Assert.*;

/**
 * Created by mageshwaranr on 02-Jan-17.
 */
public class FeatureServiceImplTest extends BasicReusableHostTestCase {

    FeatureService service;

    @Before
    public void setup() throws Throwable {
        this.host.startFactory(new FeatureDao());
        this.host.startService(new FeatureServiceImpl());
        this.host.waitForServiceAvailable(FeatureService.SELF_LINK);
        service = JaxRsServiceClient.newBuilder()
                .withHost(host)
                .withResourceInterface(FeatureService.class)
                .build();
    }

    @Test
    public void newFeature() throws Exception {
        Feature feature = new Feature();
        feature.group = "Test";
        feature.name = "newFeature";
        feature.enable = true;
        Feature saved = service.newFeature(feature).get();
        assertNotNull(saved);
        assertEquals(formId(feature.group, feature.name), selfLinkToId(saved.documentSelfLink));
        assertNotNull(saved.documentKind);
        assertTrue(saved.enable);
    }

    @Test
    public void listAll() throws Exception {
        Feature featureEnabled = new Feature();
        featureEnabled.group = "Test";
        featureEnabled.name = "listAll 1";
        featureEnabled.enable = true;
        service.newFeature(featureEnabled).get();
        Feature featureDisabled = new Feature();
        featureDisabled.group = "Test";
        featureDisabled.name = "listAll 2";
        service.newFeature(featureDisabled).get();

        Collection<Feature> features = service.listAll().get();
        assertFalse(features.isEmpty());
        MutableInt count = new MutableInt(0);

        features.forEach(feature -> {
            if (feature.name.equals(featureEnabled.name)) {
                count.increment();
                assertTrue(feature.enable);
            }
            if (feature.name.equals(featureDisabled.name)) {
                count.increment();
                assertFalse(feature.enable);
            }
        });

        assertEquals("Should be of size 2", 2, count.intValue());
    }

    @Test
    public void findByGroupAndName() throws Exception {
        Feature featureEnabled = new Feature();
        featureEnabled.group = "Test";
        featureEnabled.name = "findByGroupAndName 1";
        featureEnabled.enable = true;
        service.newFeature(featureEnabled).get();

        Feature feature = service.findByGroupAndName(featureEnabled.group, featureEnabled.name).get();
        assertNotNull(feature);
        assertTrue(feature.enable);
    }

    @Test
    public void enableFeature() throws Exception {
        Feature featureDisabled = new Feature();
        featureDisabled.group = "Test";
        featureDisabled.name = "enableFeature 1";
        service.newFeature(featureDisabled).get();

        Feature feature = service.enableFeature(featureDisabled.group, featureDisabled.name).get();
        assertTrue(feature.enable);
    }

    @Test
    public void disableFeature() throws Exception {
        Feature featureEnabled = new Feature();
        featureEnabled.group = "Test";
        featureEnabled.name = "disableFeature 1";
        featureEnabled.enable = true;
        service.newFeature(featureEnabled).get();
        Feature feature = service.disableFeature(featureEnabled.group, featureEnabled.name).get();
        assertFalse(feature.enable);
    }


}