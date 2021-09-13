package uk.gov.digital.ho.hocs.casework.api.factory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CaseCopy {
    String fromCaseType() default "";
    String toCaseType() default "";
}
