package com.danven.web_library.domain.user;

import com.danven.web_library.domain.feedback.Feedback;
import com.danven.web_library.domain.order.Order;

import javax.persistence.*;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.*;

/**
 * Represents a customer in the system.
 * Inherits personal and authentication information from {@link User}.
 * Includes additional fields and behaviors specific to customers.
 */
@Entity
@Table(name = "CUSTOMER")
public class Customer extends User {

    /**
     * Contact number for the customer.
     * Must be in international format (7–15 digits with optional +, example +1234567890).
     */
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format.")
    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;


    /**
     * Birth date of the customer.
     * Cannot be null and may be used to determine eligibility or age-based features.
     */
    @NotNull(message = "Date of birth is required.")
    @Column(name = "birth_date", nullable = false)
    private LocalDate dateOfBirth;

    /**
     * Complex embedded address object.
     */
    @NotNull(message = "Address must be provided.")
    @Embedded
    private Address address;

    /**
     * Feedback entries submitted by the customer.
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Feedback> feedbacks = new HashSet<>();

    /**
     * Orders placed by the customer.
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();


    /**
     * Default constructor required by JPA.
     */
    protected Customer() {
    }


    /**
     * Full constructor with optional surname.
     *
     * @param name          the customer's name
     * @param surname       optional surname
     * @param email         unique customer email
     * @param password      password for authentication
     * @param active        account activation status
     * @param customerPhone contact phone number
     * @param dateOfBirth   date of birth
     * @param address       embedded address
     */

    public Customer(String name, Optional<String> surname, String email, String password,
                    boolean active, String customerPhone, LocalDate dateOfBirth, Address address) {
        super(name, surname, email, password, active);
        this.customerPhone = customerPhone;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    /**
     * Alternate constructor without surname.
     *
     * @param name          the customer's name
     * @param email         unique customer email
     * @param password      password for authentication
     * @param active        account activation status
     * @param customerPhone contact phone number
     * @param dateOfBirth   date of birth
     * @param address       embedded address
     */
    public Customer(String name, String email, String password,
                    boolean active, String customerPhone, LocalDate dateOfBirth, Address address) {
        super(name, email, password, active);
        this.customerPhone = customerPhone;
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    /**
     * Adds a new order to the customer.
     *
     * @param order the order to be placed
     */

    public void placeOrder(Order order) {
        validateOrder(order);
        if (!orders.contains(order)) {
            orders.add(order);
            if (order.getCustomer() != this) {
                order.setCustomer(this); // maintain bidirectional link
            }
        }
    }

/**
 * Submits a new feedback entry.
 *
 * @param feedback the feedback to submit
 */
public void leaveFeedback(Feedback feedback) {
    validateFeedback(feedback);
    feedbacks.add(feedback);
}

/**
 * Validates that the feedback is owned by this customer.
 *
 * @param feedback the feedback to validate.
 * @throws ConstraintViolationException if null or owned by another user.
 */
private void validateFeedback(Feedback feedback) {
    if (feedback == null) {
        throw new ConstraintViolationException(
                "Feedback can't be null",
                Collections.emptySet()
        );
    }
    if (!Objects.equals(feedback.getCustomer(), this)) {
        throw new ConstraintViolationException(
                "Feedback is attached to another customer",
                Collections.emptySet()
        );
    }
}

    /**
     * INTERNAL USE ONLY. Called by Feedback when unlinking customer.
     */
    public void removeFeedback(Feedback feedback) {
        feedbacks.remove(feedback);
    }

/**
 * Validates that the given order belongs to this customer.
 *
 * @param order the order to validate.
 * @throws ConstraintViolationException if invalid or owned by another customer.
 */
private void validateOrder(Order order) {
    if (order == null) {
        throw new ConstraintViolationException("Order cannot be null", Collections.emptySet());
    }
    if (order.getCustomer() != this) {
        throw new ConstraintViolationException("This order does not belong to the current customer", Collections.emptySet());
    }
}


/** @return the customer's phone number. */
public String getCustomerPhone() {
    return customerPhone;
}

/** @param customerPhone sets the customer's phone number. */
public void setCustomerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
}

/** @return the customer's date of birth. */
public LocalDate getDateOfBirth() {
    return dateOfBirth;
}

/** @param dateOfBirth sets the customer's date of birth. */
public void setDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
}

/** @return the customer's address. */
public Address getAddress() {
    return address;
}

/** @param address sets the customer's address. */
public void setAddress(Address address) {
    this.address = address;
}

/** @return an unmodifiable set of the customer's orders. */
public Set<Order> getOrders() {
    return Collections.unmodifiableSet(orders);
}

/**
* Returns all feedback entries submitted by this customer.
* The returned set is unmodifiable to preserve encapsulation.
*
* @return unmodifiable set of feedbacks
*/
public Set<Feedback> getFeedbacks() {
    return Collections.unmodifiableSet(feedbacks);
 }

/**
 * JPA lifecycle hook method.
 * This method is automatically triggered by JPA before the entity is persisted or updated.
 * It validates key business constraints that are not covered by annotations alone.
 * @throws ConstraintViolationException if critical fields are null, invalid, or inconsistent.
 */

@PrePersist
@PreUpdate
private void validateBeforePersisting() {
    if (customerPhone == null || !customerPhone.matches("^\\+?[0-9]{7,15}$")) {
        throw new ConstraintViolationException("Customer phone number is invalid.", Collections.emptySet());
    }

    if (dateOfBirth == null || dateOfBirth.isAfter(LocalDate.now())) {
        throw new ConstraintViolationException("Invalid date of birth.", Collections.emptySet());
    }

    if (address == null) {
        throw new ConstraintViolationException("Address is required.", Collections.emptySet());
    }
}


@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Customer customer)) return false;
    if (!super.equals(o)) return false;
    return Objects.equals(customerPhone, customer.customerPhone)
            && Objects.equals(dateOfBirth, customer.dateOfBirth)
            && Objects.equals(address, customer.address);
}

@Override
public int hashCode() {
    return Objects.hash(super.hashCode(), customerPhone, dateOfBirth, address);
}

@Override
public String toString() {
    return "Customer{" +
            "customerPhone='" + customerPhone + '\'' +
            ", dateOfBirth=" + dateOfBirth +
            ", address=" + address +
            '}';
}


}
