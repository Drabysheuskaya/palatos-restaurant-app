package com.danven.web_library.domain.config.custom_validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.EnumSet;

/**
 * Validator to ensure that an EnumSet is not empty.
 */
public class NotEmptyEnumSetValidator implements ConstraintValidator<NotEmptyEnumSet, EnumSet<?>> {

    /**
     * Initializes the validator in preparation for isValid calls.
     *
     * @param constraintAnnotation the annotation instance for a given constraint declaration.
     */
    @Override
    public void initialize(NotEmptyEnumSet constraintAnnotation) {
        // No initialization required for this validator
    }

    /**
     * Implements the validation logic for EnumSet.
     *
     * @param enumSet the EnumSet instance to validate.
     * @param context context in which the constraint is evaluated.
     * @return false if the validation fails, true otherwise.
     */
    @Override
    public boolean isValid(EnumSet<?> enumSet, ConstraintValidatorContext context) {
        return enumSet != null && !enumSet.isEmpty();
    }
}
