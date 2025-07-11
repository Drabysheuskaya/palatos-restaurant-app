// src/main/java/com/darina/PalaTOS/service/OrderPersistenceService.java
// src/main/java/com.darina/PalaTOS/service/OrderPersistenceService.java
package com.danven.web_library.service;

import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.order.PaymentStatus;
import com.danven.web_library.domain.user.Customer;

import java.util.List;

public interface OrderPersistenceService {
    /**
     * Persist the given Order (and its ProductOrder children) to the database.
     */
    Order save(Order order);

    /**
     * Retrieve all past orders for a given customer, newest first.
     */
    List<Order> findByCustomer(Customer customer);

    /**
     * Load a single order by its id (throws if not found).
     */
    Order findById(Long id);

    /** mark IN_PROGRESS → CANCELED */
    void cancel(Long orderId);

    /** mark PAID → (and possibly COMPLETED) */
    void pay(Long orderId, PaymentStatus paymentStatus);

    /** bring CANCELED → NEW */
    void reactivate(Long orderId);

    /** hard‐delete from database */
    void deleteById(Long orderId);

    List<Order> findAll();


}
