package com.tteky.xenonext.util.feature;

import com.tteky.xenonext.client.QueryContract;
import com.tteky.xenonext.client.StatefulServiceContract;
import com.tteky.xenonext.jaxrs.service.JaxRsBridgeStatelessService;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static com.tteky.xenonext.client.ServiceClientUtil.newStatefulSvcContract;


/**
 * Feature service
 */
public class FeatureServiceImpl extends JaxRsBridgeStatelessService implements FeatureService {

    private StatefulServiceContract<Feature> featureSvc;
    private QueryContract queryService;

    public FeatureServiceImpl() {
        setContractInterface(FeatureService.class);
    }

    @Override
    protected void initializeInstance() {
        featureSvc = newStatefulSvcContract(getHost(), FeatureDao.FACTORY_LINK, Feature.class);
        queryService = newLocalhostContract(QueryContract.class);
    }

    @Override
    public CompletableFuture<Feature> newFeature(Feature feature) {
        feature.documentSelfLink = Feature.formId(feature.group, feature.name);
        return featureSvc.post(feature);
    }

    @Override
    public CompletableFuture<Collection<Feature>> listAll() {
        return queryService.typedODataQuery(Feature.findAllCriteria(), Feature[].class)
                .thenApply(Arrays::asList);
    }

    @Override
    public CompletableFuture<Feature> findByGroupAndName(String group, String name) {
        return featureSvc.get(Feature.formId(group, name));
    }

    // using GET now because patch,post enforces body which is not required
    @Override
    public CompletableFuture<Feature> enableFeature(String group, String name) {
        return enableFeature(group, name, true);
    }

    // using GET now because patch,post enforces body which is not required
    @Override
    public CompletableFuture<Feature> disableFeature(String group, String name) {
        return enableFeature(group, name, false);
    }

    private CompletableFuture<Feature> enableFeature(String group, String name, boolean isEnabled) {
        Feature patch = new Feature();
        patch.enable = isEnabled;
        return featureSvc.patch(Feature.formId(group, name), patch);
    }


}
