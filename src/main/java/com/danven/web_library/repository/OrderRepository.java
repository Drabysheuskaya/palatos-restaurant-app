package com.danven.web_library.repository;

import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.order.OrderStatus;
import com.danven.web_library.domain.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for accessing Order entities from the database.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Retrieves all orders placed by the customer with the given ID.
     *
     * @param customerId the unique identifier of the customer
     * @return a list of {@link Order} instances belonging to that customer
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Retrieves all orders that have the specified status.
     *
     * @param status the {@link OrderStatus} to filter by
     * @return a list of {@link Order} instances matching the given status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Retrieves all orders for the given customer, ordered by order time descending.
     * Useful for displaying a customer’s most recent orders first.
     *
     * @param customer the {@link Customer} whose orders to retrieve
     * @return a list of {@link Order} instances for that customer,
     *         sorted by {@code orderTime} in descending order
     */
    List<Order> findByCustomerOrderByOrderTimeDesc(Customer customer);
}
