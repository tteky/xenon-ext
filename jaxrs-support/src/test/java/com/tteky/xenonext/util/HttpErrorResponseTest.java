package com.tteky.xenonext.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by mages_000 on 26-Dec-16.
 */
public class HttpErrorResponseTest {


    @Test
    public void testErrorToResponseAndBack() throws Exception {
        HttpError err = null;
        try {
            throwNullPointerException();
            fail("Shouldn't reach here");
        } catch (Exception e) {
            err = new HttpError("Simulated NPE", e);
            err.addToContext("TestKey", "TestValue");
        }
        HttpErrorResponse from = HttpErrorResponse.from(err);
        HttpError httpError = from.toError();
        assertEquals(1, httpError.getContext().size());
        assertEquals("TestValue", httpError.getContext().get("TestKey"));
        assertNotNull(httpError.getCause());
        assertEquals(NullPointerException.class.getTypeName(), ((HttpError) httpError.getCause()).getOriginalType());
        httpError.printStackTrace();
    }

    private void throwNullPointerException() {
        wrapperMethod();
    }

    private void wrapperMethod() {
        String str = null;
        str.length();
    }


}