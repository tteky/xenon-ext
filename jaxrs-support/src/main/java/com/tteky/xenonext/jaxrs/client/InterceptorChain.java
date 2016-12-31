package com.tteky.xenonext.jaxrs.client;

import com.vmware.xenon.common.Operation;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The interceptors are invoked sequentially and synchronously
 */
public class InterceptorChain implements OperationInterceptor {

    private OperationInterceptor[] interceptors;

    public InterceptorChain(OperationInterceptor... interceptors) {
        this.interceptors = interceptors;
    }

    public Operation interceptBeforeComplete(Operation operation) {
        if (interceptors != null) {
            for (OperationInterceptor interceptor : interceptors) {
                operation = interceptor.interceptBeforeComplete(operation);
            }
        }
        return operation;
    }

    public Pair<Operation, Throwable> interceptAfterComplete(Operation op, Pair<Operation, Throwable> result) {
        if (interceptors != null) {
            for (OperationInterceptor interceptor : interceptors) {
                result = interceptor.interceptAfterComplete(op, result);
            }
        }
        return result;
    }
}
