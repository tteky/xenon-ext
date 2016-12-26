package com.tteky.xenonext.jaxrs.reflect;

/**
 * Created by mageshwaranr
 */
public class ParamMetadata implements Comparable<ParamMetadata> {

    private String name;
    private int parameterIndex;
    private Type type;
    private Class<?> paramterType;

    public enum Type {
        PATH, QUERY, BODY, OPERATION, HEADER, COOKIE
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public void setParameterIndex(int parameterIndex) {
        this.parameterIndex = parameterIndex;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Class<?> getParamterType() {
        return paramterType;
    }

    public void setParamterType(Class<?> paramterType) {
        this.paramterType = paramterType;
    }

    @Override
    public int compareTo(ParamMetadata other) {
        return Integer.compare(this.parameterIndex, other.parameterIndex);
    }

}
