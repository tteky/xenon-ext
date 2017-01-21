package com.tteky.xenonext.util.feature;

import com.tteky.xenonext.client.QueryContract;
import com.vmware.xenon.common.ServiceDocument;
import com.vmware.xenon.common.ServiceDocumentDescription;
import com.vmware.xenon.common.Utils;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * PODO to hold details related to a single feature
 */
public class Feature extends ServiceDocument {

    @UsageOption(option = ServiceDocumentDescription.PropertyUsageOption.REQUIRED)
    @NotNull
    public String name;

    @UsageOption(option = ServiceDocumentDescription.PropertyUsageOption.REQUIRED)
    @NotNull
    public String group;

    public String description;

    public boolean enable;

    @UsageOption(option = ServiceDocumentDescription.PropertyUsageOption.REQUIRED)
    public Strategy strategy;

    static String findAllCriteria() {
        return "documentKind" + QueryContract.EQ + Utils.toDocumentKind(Feature.class);
    }

    static String formId(String group, String name) {
        return group + "::" + name;
    }

    /**
     * Returns an optional with provided input if feature is enabled.
     * Optional will be empty if feature is disabled
     * @param input
     * @param <T>
     * @return
     */
    public <T> Optional<T> toOptional(T input) {
        if(enable)
            return Optional.of(input);
        return Optional.empty();
    }

    /**
     *  Returns an optional with boolean true if feature is enabled.
     *  Optional will be empty if feature is disabled
     * @return
     */
    public Optional<Boolean> toOptional() {
        if(enable)
            return Optional.of(true);
        return Optional.empty();
    }

    public enum Strategy {
        DEFAULT
    }

}
