package org.ntnunotif.wsnu.base.util;

/**
 * Created by tormod on 3/15/14.
 */

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(java.lang.annotation.ElementType.FIELD)
public @interface EndpointReference {
    java.lang.String type() default "uri";
}
