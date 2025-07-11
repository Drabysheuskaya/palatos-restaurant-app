package com.danven.web_library.domain.config.custom_validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure that an Optional&lt;String&gt; is not empty.
 */
@Documented
@Constraint(validatedBy = OptionalStringValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalStringNotEmpty {
    /**
     * Error message to be returned if the Optional&lt;String&gt; is empty or contains an empty string.
     *
     * @return the error message.
     */
    String message() default "Invalid value";

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
