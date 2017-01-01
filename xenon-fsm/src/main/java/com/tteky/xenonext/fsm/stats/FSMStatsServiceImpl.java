package com.tteky.xenonext.fsm.stats;

import com.tteky.xenonext.client.QueryContract;
import com.tteky.xenonext.fsm.FSMService;
import com.tteky.xenonext.fsm.FSMServiceDoc;
import com.tteky.xenonext.fsm.core.StateMachineConfig;
import com.tteky.xenonext.jaxrs.client.JaxRsServiceClient;
import com.tteky.xenonext.jaxrs.service.JaxRsBridgeStatelessService;
import com.tteky.xenonext.util.HttpError;
import com.vmware.xenon.common.OperationProcessingChain;
import com.vmware.xenon.services.common.QueryTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.vmware.xenon.common.ServiceDocument.FIELD_NAME_SELF_LINK;
import static java.util.Collections.singletonMap;

/**
 * Implementation of stats service
 */
public class FSMStatsServiceImpl extends JaxRsBridgeStatelessService implements FSMStatsService {

    private Map<String, StateMachineConfig> fsmSvcs = new HashMap<>();

    private QueryContract querySvc;

    public FSMStatsServiceImpl() {
        setContractInterface(FSMStatsService.class);
    }

    public void registerSvc(String uri, Class<? extends FSMService> svcClass) throws Exception {
        this.fsmSvcs.put(uri, svcClass.newInstance().stateMachineConfig());
    }

    @Override
    public OperationProcessingChain getOperationProcessingChain() {
        if (querySvc == null) {
            querySvc = JaxRsServiceClient.newBuilder()
                    .withHost(getHost())
                    .withResourceInterface(QueryContract.class)
                    .build();
        }
        return super.getOperationProcessingChain();
    }

    @Override
    public Collection<String> registerSvc(String uri, String className) {
        try {
            Class<? extends FSMService> aClass = Class.forName(className).asSubclass(FSMService.class);
            if (!aClass.isInterface()) {
                registerSvc(uri, aClass);
            }
        } catch (Exception e) {
            throw new HttpError("Unable to find a class named " + className, e);
        }
        return new ArrayList<>(fsmSvcs.keySet());
    }

    @Override
    public List<String> allServices() {
        return new ArrayList<>(fsmSvcs.keySet());
    }

    @Override
    public Map<String, String> svcGraphson(String uri) {
        return singletonMap(uri, fsmSvcs.get(uri).generateDotFile());
    }

    @Override
    public CompletableFuture<Map<String, String>> docGraphson(String uri) {
        QueryTask.Query query = QueryTask.Query.Builder.create()
                .addFieldClause(FIELD_NAME_SELF_LINK, uri)
                .build();
        QueryTask queryTask = QueryTask.Builder.createDirectTask()
                .addOption(QueryTask.QuerySpecification.QueryOption.EXPAND_CONTENT)
                .addOption(QueryTask.QuerySpecification.QueryOption.INCLUDE_ALL_VERSIONS)
                .setQuery(query).build();
        CompletableFuture<FSMServiceDoc[]> result = querySvc.typedQuery(queryTask, FSMServiceDoc[].class);
        return result.thenApply(fsmServiceDocs -> {
            String svcUri = uri.substring(0, uri.lastIndexOf("/"));
            String graphSon = fsmSvcs.get(svcUri).generateDotFile(fsmServiceDocs);
            return singletonMap(uri, graphSon);
        });
    }

}


