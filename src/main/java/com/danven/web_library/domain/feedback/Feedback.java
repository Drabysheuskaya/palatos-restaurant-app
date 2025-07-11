package com.danven.web_library.domain.feedback;

import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.user.Customer;

import javax.persistence.*;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

/**
 * Represents feedback submitted by a customer regarding a specific order.
 * It stores a textual comment and submission time and links to the corresponding customer and order.
 */


@Entity
@Table(name = "FEEDBACK")
public class Feedback implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    /** Text content of the feedback. Cannot be blank. */
    @NotBlank(message = "Feedback description is required.")
    @Column(name = "description", nullable = false, updatable = false)
    private String description;

    /** Timestamp of feedback submission. Immutable once set. */
    @NotNull(message = "Submission time is required.")
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    /** The order to which this feedback relates. */
    @NotNull(message = "Associated order is required.")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;


    /** The customer who submitted this feedback. */
    @NotNull(message = "Submitting customer is required.")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private Customer customer;

    /** Required by JPA */
    protected Feedback() {
    }

    /**
     * Constructs new feedback.
     *
     * @param description feedback text
     * @param submittedAt time the feedback was submitted
     * @param order       associated order
     * @param customer    submitting customer
     */
    public Feedback(String description, LocalDateTime submittedAt, Order order, Customer customer) {
        this.description = description;
        this.submittedAt = submittedAt;
        this.order = order;
        this.customer = customer;

        /**
         * Maintain bidirectional associations
         */
        order.addFeedback(this);
        customer.leaveFeedback(this);
    }

    /** @return the feedback’s database ID */
    public Long getId() {
        return id;
    }

    /** @return the feedback description text */
    public String getDescription() {
        return description;
    }


    /** @return the timestamp when feedback was submitted */
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }


    /** @return the associated order being reviewed */
    public Order getOrder() {
        return order;
    }

    /** @return the customer who submitted the feedback */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Ensures that customer link cannot be changed once set.
     * @param customer new customer reference
     * @throws ConstraintViolationException if trying to reassign ownership
     */
    public void setCustomer(Customer customer) {
        if (this.customer != customer && customer != null) {
            throw new ConstraintViolationException("Customer of feedback cannot be changed.", Collections.emptySet());
        }
        if (customer == null && this.customer != null) {
            this.customer.removeFeedback(this);
            this.customer = null;
        }
    }



    /**
     * Ensures order reference cannot be changed once set.
     * @param order new order reference
     * @throws ConstraintViolationException if reassignment is attempted
     */
    public void setOrder(Order order) {
        if (this.order != order && order != null) {
            throw new ConstraintViolationException("Order of feedback cannot be changed.", Collections.emptySet());
        }
        if (order == null && this.order != null) {
            this.order.removeFeedback(this);
            this.order = null;
        }
    }



    /**
     * Lifecycle validation method invoked automatically by JPA before persisting or updating the entity.
     * Ensures that all required fields are present and logically consistent before the entity is stored.
     * This includes non-blank description, a non-null submission timestamp not set in the future,
     * and mandatory links to both the associated customer and order.
     *
     * @throws ConstraintViolationException if any business rule or integrity constraint is violated.
     */

    @PrePersist
    @PreUpdate
    private void validateBeforePersisting() {
        if (description == null || description.isBlank()) {
            throw new ConstraintViolationException("Feedback must include a non-blank description.", Collections.emptySet());
        }
        if (submittedAt == null || submittedAt.isAfter(LocalDateTime.now())) {
            throw new ConstraintViolationException("Submitted time must be in the past or present.", Collections.emptySet());
        }
        if (customer == null) {
            throw new ConstraintViolationException("Customer must be linked.", Collections.emptySet());
        }
        if (order == null) {
            throw new ConstraintViolationException("Order must be linked.", Collections.emptySet());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Feedback)) return false;
        Feedback feedback = (Feedback) o;
        return id != null && Objects.equals(id, feedback.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
