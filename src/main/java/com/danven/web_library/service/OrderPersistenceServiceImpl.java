
package com.danven.web_library.service;

import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.order.OrderStatus;
import com.danven.web_library.domain.order.PaymentStatus;
import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link OrderPersistenceService} that provides
 * methods to save, retrieve, update, and delete {@link Order} entities.
 * Leverages {@link OrderRepository} for database interactions.
 */
@Service
public class OrderPersistenceServiceImpl implements OrderPersistenceService {

    private final OrderRepository orderRepo;

    public OrderPersistenceServiceImpl(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    /**
     * Persists the given order along with its associated ProductOrder entries.
     *
     * @param order the {@link Order} to save
     * @return the saved {@link Order} instance
     */
    @Override
    @Transactional
    public Order save(Order order) {
        // cascades ProductOrder children
        return orderRepo.save(order);
    }

    /**
     * Retrieves all orders for the specified customer, sorted by order time descending.
     *
     * @param customer the {@link Customer} whose orders to fetch
     * @return list of {@link Order} for that customer
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> findByCustomer(Customer customer) {
        // assuming you add this query method to OrderRepository:
        // List<Order> findByCustomerOrderByOrderTimeDesc(Customer customer);
        return orderRepo.findByCustomerOrderByOrderTimeDesc(customer);
    }

    /**
     * Finds an order by its ID.
     *
     * @param id the ID of the order
     * @return the {@link Order} if found
     * @throws IllegalArgumentException if no order exists with the given ID
     */
    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
    }

    /**
     * Cancels the order with the given ID.
     * Only orders in NEW and UNPAID state can be cancelled by the customer.
     *
     * @param orderId the ID of the order to cancel
     * @throws IllegalStateException if the order is not NEW or is already paid
     */
    @Override
    @Transactional
    public void cancel(Long orderId) {
        Order o = findById(orderId);
        if (o.getStatus() != OrderStatus.NEW || o.getPaymentStatus() != PaymentStatus.UNPAID) {
            throw new IllegalStateException("Only NEW and UNPAID orders can be cancelled by the customer");
        }
        o.setStatus(OrderStatus.CANCELED);
        orderRepo.save(o);
    }


    /**
     * Processes payment for the order with the given ID.
     * Updates payment status and, if already served, marks the order as COMPLETED.
     *
     * @param orderId       the ID of the order to pay
     * @param paymentStatus the new {@link PaymentStatus} to set
     */
    @Override @Transactional
    public void pay(Long orderId, PaymentStatus paymentStatus) {
        Order o = findById(orderId);
        o.setPaymentStatus(paymentStatus);
        if (o.getStatus() == OrderStatus.SERVED) {
            o.setStatus(OrderStatus.COMPLETED);
        }
        orderRepo.save(o);
    }

    /**
     * Reactivates a previously cancelled order, setting its status back to NEW.
     *
     * @param orderId the ID of the order to reactivate
     */
    @Override @Transactional
    public void reactivate(Long orderId) {
        Order o = findById(orderId);
        if (o.getStatus() == OrderStatus.CANCELED) {
            o.setStatus(OrderStatus.NEW);
            orderRepo.save(o);
        }
    }

    /**
     * Deletes the order with the specified ID.
     *
     * @param orderId the ID of the order to delete
     */
    @Override @Transactional
    public void deleteById(Long orderId) {
        orderRepo.deleteById(orderId);
    }

    /**
     * Retrieves all orders in the system.
     *
     * @return list of all {@link Order} entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepo.findAll();
    }

}
