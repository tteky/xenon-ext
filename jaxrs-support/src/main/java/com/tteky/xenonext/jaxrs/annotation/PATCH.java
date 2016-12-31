package com.tteky.xenonext.jaxrs.annotation;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.NameBinding;
import java.lang.annotation.*;

/**
 * Annotation to represent HTTP Method PATCH
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("PATCH")
@Documented
@NameBinding
public @interface PATCH {
}
