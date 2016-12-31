package com.tteky.xenonext.jaxrs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to represent that a particular method argument / parameter can be populated using
 * Operation.getBody() content. Note that, jsr-303 constraint validation will be applied on the body if used on the POJO.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationBody {
}
