package com.danven.web_library.domain.config.custom_validators;

import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.order.OrderStatus;
import com.danven.web_library.domain.order.PaymentStatus;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator to ensure consistency of an Order based on its status and required fields.
 */
public class OrderTypeValidator implements ConstraintValidator<ValidOrderTypes, Order> {

    @Override
    public void initialize(ValidOrderTypes constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(Order order, ConstraintValidatorContext context) {
        if (order == null) return true; // Avoid null pointer exceptions

        boolean valid = true;

        OrderStatus status = order.getStatus();
        PaymentStatus paymentStatus = order.getPaymentStatus();

        // COMPLETED orders must have PAID payment status
        if (status == OrderStatus.COMPLETED && paymentStatus != PaymentStatus.PAID) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Payment must be PAID when order is COMPLETED")
                    .addPropertyNode("paymentStatus")
                    .addConstraintViolation();
            valid = false;
        }

        // Final price must not be negative
        double finalPrice = order.calculateFinalPrice();
        if (Double.compare(finalPrice, 0.0) < 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Final price must be non-negative")
                    .addPropertyNode("finalPrice")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
