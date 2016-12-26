package com.tteky.xenonext.jaxrs.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mageshwaranr.
 * <p>
 * Annotation to represent that a particular method argument / parameter can be populated using
 * Operation.getBody() content. Note that, jsr-303 constraint validation will be applied if used.
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationBody {
}
