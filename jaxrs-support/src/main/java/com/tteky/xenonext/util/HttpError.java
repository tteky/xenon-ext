package com.tteky.xenonext.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Exception class to wrap any HTTP based errors
 */
public class HttpError extends RuntimeException {

    // external facing error code
    // ui should be able to fetch a user friend message using this error-code
    // error code should follow pattern [M][C][E]
    // where M-> Module Code. Should be 2 digit and between 10 > M < 100
    //       C -> Client Response hint code,  1 for retry
    //       E -> Server side error code. Should be 2 digit and between 10 > M < 100
    //Reserved error code[55055] for internal errors
    private int errorCode;

    //Message useful for a developer to debug further. This is not for end-user consumption
    private String developerMessage;

    //Context information on when this error occurred. Can include input arguments, document self link etc.,
    private Map<String, Object> context = new HashMap<>();

    private String originalType;

    public HttpError(int errorCode) {
        this.errorCode = errorCode;
    }

    public HttpError(Throwable e, int errorCode) {
        super(e);
        this.errorCode = errorCode;
    }

    public HttpError(int errorCode, String developerMessage) {
        super(developerMessage);
        this.errorCode = errorCode;
        this.developerMessage = developerMessage;
    }

    public HttpError(String developerMessage, Throwable e) {
        super(developerMessage, e);
        this.errorCode = 55055; // internal error
        this.developerMessage = developerMessage;
    }

    public HttpError(Throwable e, int errorCode, String developerMessage) {
        super(developerMessage, e);
        this.errorCode = errorCode;
        this.developerMessage = developerMessage;
    }


    public HttpError(int errorCode, String developerMessage, Map<String, Object> context) {
        this.errorCode = errorCode;
        this.developerMessage = developerMessage;
        this.context = context;
    }

    public HttpError(Throwable e, int errorCode, String developerMessage, Map<String, Object> context) {
        super(e);
        this.errorCode = errorCode;
        this.developerMessage = developerMessage;
        this.context = context;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public void setDeveloperMessage(String developerMessage) {
        this.developerMessage = developerMessage;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    /**
     * Builder style construct to add context information on when this error occured
     *
     * @param key
     * @param value
     * @return
     */
    public HttpError addToContext(String key, Object value) {
        this.context.put(key, value);
        return this;
    }

    public String getOriginalType() {
        return originalType;
    }

    public void setOriginalType(String originalType) {
        this.originalType = originalType;
    }

    @Override
    public String toString() {
        return "HttpError{" +
                "errorCode=" + errorCode +
                ", developerMessage='" + developerMessage + '\'' +
                ", context=" + context +
                ", originalType='" + originalType + '\'' +
                '}';
    }
}