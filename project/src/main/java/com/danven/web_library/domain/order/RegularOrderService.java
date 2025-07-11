package com.danven.web_library.domain.order;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * A standard implementation of {@link OrderService} representing a regular order handling
 * service without any discounts or special promotional rules.
 * This service applies a 0% discount rate and is used for orders processed under the default terms.
 */
@Entity
@Table(name = "REGULAR_ORDER_SERVICE")
public class RegularOrderService extends OrderService {

    /**
     * Default constructor required by JPA. Initializes with a default service name
     * and zero discount rate.
     */
    protected RegularOrderService() {}

    /**
     * Constructs a new RegularOrderService with a custom service name.
     * The discount rate remains 0%.
     *
     * @param serviceName the human-readable name of this service; must not be null or blank
     * @throws IllegalArgumentException if {@code serviceName} is null or empty
     */
    public RegularOrderService(String serviceName) {
        super(serviceName, 0.0);
    }

    /**
     * Applies this service to the given order. This implementation does not modify
     * the order's total (no discounts applied) but marks it as processed by this service.
     *
     * @param order the {@link Order} to process; must not be null
     * @throws IllegalArgumentException if {@code order} is null
     */
    @Override
    public void applyToOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        // No discount logic; simply associate the service with the order
        order.setOrderService(this);
    }
}
