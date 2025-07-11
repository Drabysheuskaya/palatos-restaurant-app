package com.danven.web_library.domain.order;

import javax.persistence.*;
import javax.validation.ValidationException;

/**
 * Abstract base class for order services (e.g., Holiday or Regular).
 * Applies service-related business logic to orders.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "ORDER_SERVICE")
public abstract class OrderService implements IOrderService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_service_id")
    private Long id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "discount_rate")
    private double discountRate;

    protected OrderService() {}

    protected OrderService(String serviceName, double discountRate) {
        if (discountRate < 0 || discountRate > 1) {
            throw new ValidationException("Discount rate must be between 0 and 1.");
        }
        this.serviceName = serviceName;
        this.discountRate = discountRate;
    }

    public Long getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDiscountRate(double discountRate) {
        if (discountRate < 0 || discountRate > 1) {
            throw new ValidationException("Discount rate must be between 0 and 1.");
        }
        this.discountRate = discountRate;
    }

    /**
     * Applies this service's business logic to the given order.
     */
    public abstract void applyToOrder(Order order);
}
