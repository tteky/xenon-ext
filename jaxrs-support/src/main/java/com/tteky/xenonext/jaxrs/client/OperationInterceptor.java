package com.tteky.xenonext.jaxrs.client;

import com.vmware.xenon.common.Operation;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Created by mageshwaranr
 * Please do not maintain state (instance level variables) with in interceptor
 */
public interface OperationInterceptor {

    default Operation interceptBeforeComplete(Operation op) {
        return op;
    }

    default Pair<Operation, Throwable> interceptAfterComplete(Operation op, Pair<Operation, Throwable> result) {
        return result;
    }
}
