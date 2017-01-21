package com.tteky.xenonext.util.feature;

import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.StatefulService;
import com.vmware.xenon.common.Utils;

import static com.vmware.xenon.common.Service.ServiceOption.*;

/**
 * Basic stateful service to maintain feature flip
 * For internal use only. Use FeatureService instead.
 */
public class FeatureDao extends StatefulService {

    public static final String FACTORY_LINK = "/common/persistence/feature";

    public FeatureDao() {
        super(Feature.class);
        super.toggleOption(PERSISTENCE, true);
        super.toggleOption(OWNER_SELECTION, true);
        super.toggleOption(REPLICATION, true);
    }


    @Override
    public void handleStart(Operation post) {
        try {
            Feature feature = post.getBody(Feature.class);
            if (feature.strategy == null) {
                feature.strategy = Feature.Strategy.DEFAULT;
            }
            Utils.validateState(getStateDescription(), feature);
            super.handleStart(post);
        } catch (Exception e) {
            post.fail(e);
        }
    }

    @Override
    public void handlePatch(Operation patch) {
        Feature recv = patch.getBody(Feature.class);
        Feature current = getState(patch);
        current.enable = recv.enable;
        if (recv.strategy != null) {
            current.strategy = recv.strategy;
        }
        setState(patch, current);
        patch.complete();
    }


}
