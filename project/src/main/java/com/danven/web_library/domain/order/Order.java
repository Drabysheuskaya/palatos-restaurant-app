package com.danven.web_library.domain.order;

import com.danven.web_library.domain.feedback.Feedback;
import com.danven.web_library.domain.product.Product;
import com.danven.web_library.domain.product.ProductOrder;
import com.danven.web_library.domain.user.Customer;

import javax.persistence.*;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a customer's placed order, containing products and associated services.
 * Supports calculations for pricing, service fees, and maintains integrity constraints.
 */
@Entity
@Table(name = "ORDERS")
public class Order implements Serializable {

    /**
     * Primary key identifier of the order.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    /**
     * Table number where the order was placed. Must be >= 1.
     */
    @Column(name = "table_number", nullable = false)
    @Min(value = 1, message = "Table number must be at least 1.")
    private int tableNumber;

    /**
     * Timestamp when the order was created. Immutable once set.
     */
    @Column(name = "order_time", nullable = false, updatable = false)
    private LocalDateTime orderTime;

    /**
     * Current lifecycle status of the order (e.g., NEW, IN_PROGRESS, SERVED).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    /**
     * Payment status of the order (UNPAID, PAID).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    /**
     * Optional notes or special instructions for the order.
     */
    @Column(name = "notes")
    private String notes;

    /**
     * Customer who placed the order. Immutable once assigned.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private Customer customer;

    /**
     * Service applied to this order (e.g., regular or holiday promotions).
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_service_id", nullable = false)
    private OrderService orderService;

    /**
     * Feedback entries associated with this order.
     */
    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    private Set<Feedback> feedbacks = new HashSet<>();

    /**
     * Ordered product lines, capturing quantity and historic prices.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductOrder> productOrders = new HashSet<>();


    private static final double DEFAULT_SERVICE_FEE_RATE = 0.10;

    /**
     * Default constructor initializing a new, unpaid order in NEW status.
     */
    public Order() {
        this.status        = OrderStatus.NEW;
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    /**
     * Constructs an order with all required fields.
     * Applies the specified service immediately.
     *
     * @param tableNumber   table number, >=1
     * @param orderTime     order creation timestamp, not in the future
     * @param status        initial status
     * @param paymentStatus initial payment status
     * @param notes         optional notes
     * @param customer      the placing customer
     * @param orderService  the service to apply
     * @throws ValidationException for invalid parameters
     */
    public Order(int tableNumber,
                 LocalDateTime orderTime,
                 OrderStatus status,
                 PaymentStatus paymentStatus,
                 String notes,
                 Customer customer,
                 OrderService orderService) {
        this.tableNumber   = tableNumber;
        this.orderTime     = orderTime;
        this.status        = status;
        this.paymentStatus = paymentStatus;
        this.notes         = notes;
        setCustomer(customer);
        setOrderService(orderService);
        applyOrderService();
    }

    /**
     * Calculates the total price of all ordered products.
     *
     * @return sum of (unit price * quantity)
     */
    public double calculateTotalAmount() {
        return productOrders.stream()
                .map(ProductOrder::calculateSubtotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .doubleValue();
    }


    /**
     * Calculates the restaurant-wide service fee at 10%.
     *
     * @return service fee amount
     */
    public double calculateServiceFee() {
        return calculateTotalAmount() * 0.10; // 10% restaurant-wide service fee
    }

    /**
     * Calculates final payable amount including service fee and any applicable discounts.
     *
     * @return final order price
     */
    /**
     * Calculates the final payable amount by:
     * 1) summing all product subtotals,
     * 2) adding the restaurant service fee (10%),
     * 3) then applying the configured service’s discount rate, if and only if it’s applicable.
     *
     * @return the total after fee and any service‐level discount
     */
    public double calculateFinalPrice() {
        // 1) sum of product subtotals
        double total      = calculateTotalAmount();
        // 2) service fee (10% of subtotal)
        double serviceFee = calculateServiceFee();
        // 3) combined total before discount
        double base       = total + serviceFee;

        // default no discount
        double discountRate = 0.0;

        // if the service offers a rate >0, check applicability
        if (orderService.getDiscountRate() > 0) {
            if (orderService instanceof HolidayOrderService holiday) {
                // only apply if we're in the holiday window
                if (holiday.isHolidayApplicable(this)) {
                    discountRate = holiday.getDiscountRate();
                }
            } else {
                // any other service with a non-zero rate (e.g. future DishService)
                discountRate = orderService.getDiscountRate();
            }
        }

        // 4) apply the rate to the full amount
        return base * (1 - discountRate);
    }


    /**
     * Calculates the total quantity of items in the order.
     *
     * @return sum of all product quantities
     */
    public int calculateTotalQuantity() {
        return productOrders.stream()
                .mapToInt(ProductOrder::getAmount)
                .sum();
    }

    /**
     * Adds a product line to this order.
     * Validates that the ProductOrder references this order.
     *
     * @param productOrder the product line to add
     * @throws ValidationException if null or mismatched order
     */
    public void addProductOrder(ProductOrder productOrder) {
        validateProductOrder(productOrder);
        if (productOrder.getOrder() != this) {
            throw new ValidationException("ProductOrder must reference this Order.");
        }
        productOrders.add(productOrder);
    }

    /**
     * Removes a product line and unlinks it from both sides.
     *
     * @param productOrder product line to remove
     */
    public void removeProductOrder(ProductOrder productOrder) {
        if (productOrders.remove(productOrder)) {
            productOrder.removeProductOrder(); // unlink both sides safely
        }
    }


    private void validateProductOrder(ProductOrder productOrder) {
        if (productOrder == null) {
            throw new ValidationException("ProductOrder cannot be null.");
        }
    }


    /**
     * Adds a feedback entry to this order.
     *
     * @param feedback feedback to add
     * @throws ValidationException if null or mismatched order
     */
    public void addFeedback(Feedback feedback) {
        validateFeedback(feedback);
        feedbacks.add(feedback);
    }

    /**
     * Removes a feedback entry. Intended for internal use.
     *
     * @param feedback feedback to remove
     */
    public void removeFeedback(Feedback feedback) {
        feedbacks.remove(feedback);
    }


    private void validateProduct(Product product) {
        if (product == null) {
            throw new ValidationException("Product cannot be null.");
        }
    }

    private void validateFeedback(Feedback feedback) {
        if (feedback == null) {
            throw new ValidationException("Feedback cannot be null.");
        }
        if (!this.equals(feedback.getOrder())) {
            throw new ValidationException("Feedback is associated with another order.");
        }
    }

    public Long getId() {
        return id;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    /**
     * Sets the table number. Must be >=1.
     *
     * @param tableNumber table number
     */
    public void setTableNumber(int tableNumber) {
        if (tableNumber < 1) {
            throw new ValidationException("Table number must be at least 1.");
        }
        this.tableNumber = tableNumber;
    }


    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        if (status == null) {
            throw new ValidationException("Order status must not be null.");
        }
        this.status = status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new ValidationException("Payment status must not be null.");
        }
        this.paymentStatus = paymentStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Customer getCustomer() {
        return customer;
    }

    /**
     * Sets the customer for this order. Immutable once set.
     *
     * @param customer the placing customer
     * @throws ValidationException if null or changing existing
     */
    public void setCustomer(Customer customer) {
        if (customer == null) {
            throw new ValidationException("Customer must not be null.");
        }
        if (this.customer != null && !this.customer.equals(customer)) {
            throw new ValidationException("Order customer cannot be changed.");
        }
        this.customer = customer;
        if (!customer.getOrders().contains(this)) {
            customer.placeOrder(this); // safe call — idempotent
        }
    }


    public OrderService getOrderService() {
        return orderService;
    }

    /**
     * Sets the service for this order.
     *
     * @param orderService the service to apply
     * @throws ValidationException if null
     */
    public void setOrderService(OrderService orderService) {
        if (orderService == null) {
            throw new ValidationException("Order service must not be null.");
        }
        this.orderService = orderService;
    }

    public void processPayment() {
        if (paymentStatus == PaymentStatus.PAID) {
            throw new ValidationException("Order has already been paid.");
        }
        if (status != OrderStatus.IN_PROGRESS && status != OrderStatus.SERVED) {
            throw new ValidationException("Order must be in progress or served before payment.");
        }
        this.paymentStatus = PaymentStatus.PAID;

        if (status == OrderStatus.SERVED) {
            this.status = OrderStatus.COMPLETED;
        }
    }

    /**
     * Applies the configured OrderService to this order.
     */
    public void applyOrderService() {
        if (orderService != null) {
            orderService.applyToOrder(this);
        }
    }

    public Set<Product> getProductsFromProductOrders() {
        return productOrders.stream()
                .map(ProductOrder::getProduct)
                .collect(Collectors.toSet());
    }

    public void setOrderTime(LocalDateTime orderTime) {
        if (orderTime == null || orderTime.isAfter(LocalDateTime.now())) {
            throw new ValidationException("Order time must be set and not in the future.");
        }
        this.orderTime = orderTime;
    }


    public Set<ProductOrder> getProductOrders() {
        return Collections.unmodifiableSet(productOrders);
    }


    public Set<Feedback> getFeedbacks() {
        return Collections.unmodifiableSet(feedbacks);
    }

    /**
     * Ensures required fields and constraints before persisting or updating.
     */
    @PrePersist
    @PreUpdate
    private void validateOrderBeforeSave() {
        if (orderTime == null || orderTime.isAfter(LocalDateTime.now())) {
            throw new ValidationException("Order time must be set and not in the future.");
        }
        if (status == null) {
            throw new ValidationException("Order status is required.");
        }
        if (paymentStatus == null) {
            throw new ValidationException("Payment status is required.");
        }
        if (customer == null) {
            throw new ValidationException("Customer must not be null.");
        }
        if (orderService == null) {
            throw new ValidationException("Order service must not be null.");
        }
    }

    /**
     * Equality based on database identifier.
     *
     * @param o other object
     * @return true if same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order order)) return false;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
