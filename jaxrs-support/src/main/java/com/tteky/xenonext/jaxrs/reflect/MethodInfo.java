package com.tteky.xenonext.jaxrs.reflect;

import com.vmware.xenon.common.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Method info corresponding to a JaxRs Annotated HTTP method
 */
public class MethodInfo {

    private Method method;
    private String name;
    private List<ParamMetadata> parameters = new ArrayList<>();
    private String uriPath;
    private Service.Action action;
    private Map<String, Integer> pathParamsVsUriIndex;
    private boolean asyncApi;
    private Class<?> returnType;
    private Type type;

    public MethodInfo(Method method) {
        this.method = method;
        this.name = method.getName();
    }

    public String getName() {
        return name;
    }

    public List<ParamMetadata> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParamMetadata> parameters) {
        this.parameters = parameters;
    }

    public String getUriPath() {
        return uriPath;
    }

    public void setUriPath(String uriPath) {
        this.uriPath = uriPath;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Service.Action getAction() {
        return action;
    }

    public void setAction(Service.Action action) {
        this.action = action;
    }

    public Map<String, Integer> getPathParamsVsUriIndex() {
        return pathParamsVsUriIndex;
    }

    public void setPathParamsVsUriIndex(Map<String, Integer> pathParamsVsUriIndex) {
        this.pathParamsVsUriIndex = pathParamsVsUriIndex;
    }

    public boolean isAsyncApi() {
        return asyncApi;
    }

    public void setAsyncApi(boolean asyncApi) {
        this.asyncApi = asyncApi;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
