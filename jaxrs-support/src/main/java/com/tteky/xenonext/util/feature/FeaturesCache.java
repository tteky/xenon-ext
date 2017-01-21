package com.tteky.xenonext.util.feature;

import com.vmware.xenon.common.*;
import com.vmware.xenon.services.common.QueryTask;
import com.vmware.xenon.services.common.ServiceUriPaths;

import java.net.URI;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Created by mageshwaranr on 20-Jan-17.
 * Represents a Cache used to store the state of all features {@link FeatureService} hosted in local host
 * Also, holds responsibility of keeping this up to date.
 * This is a inspired and ported from GatewayCache to suit {@link Feature} needs
 */
public class FeaturesCache {

    private final ConcurrentHashMap<String, Feature> cache = new ConcurrentHashMap<>();
    private ServiceHost host;
    private static FeaturesCache instance = new FeaturesCache();

    private FeaturesCache() {
    }

    public static FeaturesCache instance() {
        return instance;
    }

    public static Feature findFeature(Feature feature) {
        return findFeature(feature.group, feature.name);
    }

    public static Feature findFeature(String group, String name) {
      return instance.cache.computeIfAbsent(Feature.formId(group, name), (id) -> {
            Feature feature = new Feature();
            feature.group = group;
            feature.name = name;
            return feature;
        });
    }

    /**
     * Constructs a FeaturesCache instance and returns it.
     */
    public static FeaturesCache initialize(ServiceHost host) {
        instance.host = host;
        instance.start();
        return instance;
    }

    /**
     * Starts the cache instance by creating a continuous query task to listen for updates.
     * <p>
     * The continuous query task created here is a "local" query-task that is
     * dedicated for keeping the cache on the local node up-to-date. This simplifies
     * the design considerably for dealing with node-group changes. Each
     * node as it starts, creates a continuous query on the local index. As
     * the configuration state gets propagated through replication or synchronization
     * the local cache gets updated as well.
     */
    private void start() {
        try {
            QueryTask continuousQueryTask = createContinuousQueryTask();
            URI queryTaskUri = UriUtils.buildUri(
                    this.host.getUri(), ServiceUriPaths.CORE_LOCAL_QUERY_TASKS);
            Operation.createPost(queryTaskUri)
                    .setBody(continuousQueryTask)
                    .setReferer(this.host.getUri())
                    .setCompletion((o, e) -> {
                        if (e != null) {
                            this.host.log(Level.SEVERE,
                                    "Failed to setup continuous query. Failure: %s", e.toString());
                            return;
                        }
                        QueryTask rsp = o.getBody(QueryTask.class);
                        createSubscription(this.host, rsp);
                    }).sendWith(this.host);

        } catch (Exception e) {
            this.host.log(Level.SEVERE, e.toString());
        }
    }

    private QueryTask createContinuousQueryTask() {
        QueryTask.Query query = QueryTask.Query.Builder.create()
                .addKindFieldClause(Feature.class, QueryTask.Query.Occurance.SHOULD_OCCUR)
                .build();

        EnumSet<QueryTask.QuerySpecification.QueryOption> queryOptions = EnumSet.of(
                QueryTask.QuerySpecification.QueryOption.EXPAND_CONTENT,
                QueryTask.QuerySpecification.QueryOption.CONTINUOUS);

        QueryTask queryTask = QueryTask.Builder
                .create()
                .addOptions(queryOptions)
                .setQuery(query)
                .build();
        queryTask.documentExpirationTimeMicros = Long.MAX_VALUE;
        return queryTask;
    }

    private void createSubscription(ServiceHost currentHost, QueryTask queryTask) {
        try {
            // Create subscription using replay state to bootstrap the cache.
            ServiceSubscriptionState.ServiceSubscriber sr = ServiceSubscriptionState
                    .ServiceSubscriber.create(true);

            Operation subscribe = Operation
                    .createPost(UriUtils.buildUri(currentHost.getUri(), queryTask.documentSelfLink))
                    .setReferer(currentHost.getUri())
                    .setCompletion((o, e) -> {
                        if (e != null) {
                            currentHost.log(Level.SEVERE,
                                    "Failed to subscribe to the continuous query. Failure: %s", e.toString());
                            return;
                        }
                        currentHost.log(Level.INFO,
                                "Subscription started successfully");
                    });
            currentHost.startSubscriptionService(subscribe, handleFeatureUpdates(), sr);
        } catch (Exception e) {
            currentHost.log(Level.SEVERE, e.toString());
        }
    }

    private Consumer<Operation> handleFeatureUpdates() {
        return (notifyOp) -> {
            notifyOp.complete();
            QueryTask queryTask;
            try {
                queryTask = notifyOp.getBody(QueryTask.class);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
            if (queryTask.results != null && queryTask.results.documents.size() > 0) {
                for (Map.Entry<String, Object> entry : queryTask.results.documents.entrySet()) {
                    String documentKind = Utils
                            .fromJson(entry.getValue(), ServiceDocument.class)
                            .documentKind;
                    if (documentKind.equals(Utils.buildKind(Feature.class))) {
                        Feature obj = Utils.fromJson(entry.getValue(),
                                Feature.class);
                        handleFeatureUpdate(obj);
                    }
                }
            }
        };
    }

    private void handleFeatureUpdate(Feature feature) {
        if (feature.documentUpdateAction.equals(Service.Action.DELETE.toString())) {
            this.cache.remove(Feature.formId(feature.group, feature.name));
            this.host.log(Level.SEVERE, "Feature is removed.  %s", feature);
        } else {
            this.cache.put(Feature.formId(feature.group, feature.name), feature);
            this.host.log(Level.INFO, "Feature is created/updated %s ", feature);
        }
    }


}
