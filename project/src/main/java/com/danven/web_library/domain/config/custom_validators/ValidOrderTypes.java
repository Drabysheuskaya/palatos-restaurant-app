package com.danven.web_library.domain.config.custom_validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure business rule consistency in Order based on its status.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderTypeValidator.class)
@Documented
public @interface ValidOrderTypes {

    /**
     * Default error message if the validation fails.
     *
     * @return error message.
     */
    String message() default "Invalid order configuration based on status";

    /**
     * Validation groups.
     *
     * @return groups.
     */
    Class<?>[] groups() default {};

    /**
     * Payload for attaching metadata.
     *
     * @return payload.
     */
    Class<? extends Payload>[] payload() default {};
}
