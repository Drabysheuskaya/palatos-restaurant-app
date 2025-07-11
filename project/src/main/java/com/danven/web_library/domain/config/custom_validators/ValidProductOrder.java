package com.danven.web_library.domain.config.custom_validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom validation annotation to ensure the validity of a ProductOrder.
 * <p>
 * It validates constraints such as:
 * - Positive amount.
 * - No modification if the parent order is already SERVED or COMPLETED.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProductOrderValidator.class)
@Documented
public @interface ValidProductOrder {

    /**
     * Default error message when validation fails.
     *
     * @return the error message.
     */
    String message() default "Invalid product order";

    /**
     * Allows specifying validation groups.
     *
     * @return the groups.
     */
    Class<?>[] groups() default {};

    /**
     * Allows specifying custom payloads.
     *
     * @return the payload.
     */
    Class<? extends Payload>[] payload() default {};
}
