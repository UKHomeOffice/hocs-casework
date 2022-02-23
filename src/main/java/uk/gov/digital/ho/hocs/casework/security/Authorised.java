package uk.gov.digital.ho.hocs.casework.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import static uk.gov.digital.ho.hocs.casework.security.AccessLevel.UNSET;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorised {
    AccessLevel accessLevel() default UNSET;

    AccessLevel[] permittedLowerLevels() default {};
}