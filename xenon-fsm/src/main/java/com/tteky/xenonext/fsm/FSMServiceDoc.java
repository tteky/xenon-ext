package com.tteky.xenonext.fsm;

import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceDocumentDescription;

import java.util.Map;

/**
 * Created by mageshwaranr on 01-Dec-16.
 * <p>
 * Sub-classes can add additional arguments as needed. Sub-class of FSMService shouldn't directly
 * manipulate the fields defined in this class
 */
public class FSMServiceDoc extends ServiceDocument {

    /**
     * Tracks progress of the task. Represents the current state
     */
    @PropertyOptions(usage = ServiceDocumentDescription.PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL,
            indexing = ServiceDocumentDescription.PropertyIndexingOption.EXPAND)
    public String state;

    /**
     * The latest trigger, for which the execution should happen.
     */
    @PropertyOptions(usage = ServiceDocumentDescription.PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL,
            indexing = ServiceDocumentDescription.PropertyIndexingOption.EXPAND)
    public String trigger;

    /**
     * If set, on expiry, self patch this trigger
     */
    @PropertyOptions(usage = ServiceDocumentDescription.PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL)
    public Map<String, Long> expiryTriggerAndDelays;


    /**
     * State on which expiry is applicable
     */
    @PropertyOptions(usage = ServiceDocumentDescription.PropertyUsageOption.AUTO_MERGE_IF_NOT_NULL)
    public String expireFromState;

}