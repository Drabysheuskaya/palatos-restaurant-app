package com.danven.web_library.domain.config.custom_validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

/**
 * Validator to ensure that an Optional&lt;String&gt; is not empty or contains a non-empty string.
 */
public class OptionalStringValidator implements ConstraintValidator<OptionalStringNotEmpty, Optional<String>> {

    /**
     * Initializes the validator in preparation for isValid calls.
     *
     * @param constraintAnnotation the annotation instance for a given constraint declaration.
     */
    @Override
    public void initialize(OptionalStringNotEmpty constraintAnnotation) {
        // No initialization required for this validator
    }

    /**
     * Implements the validation logic for Optional&lt;String&gt;.
     *
     * @param value the Optional&lt;String&gt; instance to validate.
     * @param context context in which the constraint is evaluated.
     * @return false if the validation fails, true otherwise.
     */
    @Override
    public boolean isValid(Optional<String> value, ConstraintValidatorContext context) {
        return value.map(s -> s.trim().length() > 0).orElse(true);
    }
}
