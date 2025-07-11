package com.danven.web_library.domain.config.custom_validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure that an EnumSet is not empty.
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEmptyEnumSetValidator.class)
@Documented
public @interface NotEmptyEnumSet {
    /**
     * Error message to be returned if the EnumSet is empty.
     *
     * @return the error message.
     */
    String message() default "At least one role should be chosen";

    /**
     * Groups to which this constraint belongs.
     *
     * @return the groups.
     */
    Class<?>[] groups() default {};

    /**
     * Payload for clients to specify additional information about the validation failure.
     *
     * @return the payload.
     */
    Class<? extends Payload>[] payload() default {};
}
