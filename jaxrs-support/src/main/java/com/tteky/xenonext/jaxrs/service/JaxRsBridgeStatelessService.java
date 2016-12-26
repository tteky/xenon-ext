package com.tteky.xenonext.jaxrs.service;

import com.vmware.xenon.common.OperationProcessingChain;
import com.vmware.xenon.common.RequestRouter;
import com.vmware.xenon.common.StatelessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mageshwaranr on 9/16/2016.
 */
public class JaxRsBridgeStatelessService extends StatelessService {

    protected Logger log = LoggerFactory.getLogger(getClass());

    public JaxRsBridgeStatelessService() {
        super.toggleOption(ServiceOption.URI_NAMESPACE_OWNER, true);
    }

    private Class<?> contractInterface;

    @Override
    public OperationProcessingChain getOperationProcessingChain() {
        if (super.getOperationProcessingChain() != null) {
            return super.getOperationProcessingChain();
        }
        final OperationProcessingChain opProcessingChain = new OperationProcessingChain(this);
        RequestRouter requestRouter = RequestRouterBuilder.parseJaxRsAnnotations(this, contractInterface);
        opProcessingChain.add(requestRouter);
        setOperationProcessingChain(opProcessingChain);
        return opProcessingChain;
    }

    /**
     * The contract interface implemented by this service which has declared methods with JaxRsAnnotations
     *
     * @param iFace
     */
    protected void setContractInterface(Class<?> iFace) {
        this.contractInterface = iFace;
    }


}
