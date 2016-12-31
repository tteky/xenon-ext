package com.tteky.xenonext.util;

import com.vmware.xenon.common.ServiceErrorResponse;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static com.vmware.xenon.common.Utils.buildKind;

/**
 * Response body class of HttpError
 */
public class HttpErrorResponse extends ServiceErrorResponse {

    public static HttpErrorResponse from(Throwable e) {
        HttpErrorResponse rsp = new HttpErrorResponse();
        if (e instanceof HttpError) {
            HttpError vrbcHttpError = (HttpError) e;
            rsp.errorCode = vrbcHttpError.getErrorCode();
            rsp.context = vrbcHttpError.getContext();
            rsp.message = vrbcHttpError.getDeveloperMessage();
            rsp.stackTraceElements = vrbcHttpError.getStackTrace();
        } else {
            rsp.errorCode = 55055; //Reserved error code
            rsp.message = e.getMessage();
            rsp.stackTraceElements = e.getStackTrace();
        }
        if (Objects.nonNull(e.getCause()) && e != e.getCause()) {
            rsp.cause = from(e.getCause());
        }
        rsp.type = e.getClass().getTypeName();
        rsp.statusCode = 500;
        rsp.documentKind = buildKind(HttpErrorResponse.class);
        fillStacktrace(rsp, e);
        return rsp;
    }

    public static void fillStacktrace(ServiceErrorResponse rsp, Throwable e) {
        rsp.stackTrace = new ArrayList<>();
        for (StackTraceElement se : e.getStackTrace()) {
            rsp.stackTrace.add(se.toString());
        }
    }

    public HttpError toError() {
        HttpError error;
        if (this.cause == null) {
            error = new HttpError(this.errorCode, this.message);
        } else {
            error = new HttpError(this.cause.toError(), this.errorCode, this.message);
        }
        if (this.context != null) {
            error.setContext(this.context);
        }
        error.setDeveloperMessage(this.message);
        if (this.stackTraceElements != null) {
            error.setStackTrace(this.stackTraceElements);
        }
        error.setOriginalType(this.type);
        return error;
    }


    public Map<String, Object> context;
    public StackTraceElement[] stackTraceElements;
    public HttpErrorResponse cause;
    public String type;
}
