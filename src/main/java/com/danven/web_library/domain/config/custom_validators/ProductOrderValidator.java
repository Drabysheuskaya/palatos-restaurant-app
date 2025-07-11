package com.danven.web_library.domain.config.custom_validators;

import com.danven.web_library.domain.product.ProductOrder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for ProductOrder to ensure order logic integrity.
 */
public class ProductOrderValidator implements ConstraintValidator<ValidProductOrder, ProductOrder> {

    @Override
    public void initialize(ValidProductOrder constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(ProductOrder productOrder, ConstraintValidatorContext context) {
        boolean valid = true;

        // Amount must be positive
        if (productOrder.getAmount() <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Amount must be greater than 0")
                    .addPropertyNode("amount")
                    .addConstraintViolation();
            valid = false;
        }

        // Prevent modifications if the order is already served
        if (productOrder.getOrder() != null && productOrder.getOrder().getStatus().isServed()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Cannot modify items in a served order")
                    .addPropertyNode("order")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
