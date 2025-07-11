package com.danven.web_library.repository;

import com.danven.web_library.domain.feedback.Feedback;
import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for managing Feedback entities.
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * Find all feedbacks submitted by a specific customer.
     *
     * @param customer the customer who submitted feedback
     * @return list of feedback
     */
    List<Feedback> findAllByCustomer(Customer customer);

    /**
     * Find all feedbacks associated with a specific order.
     *
     * @param order the order being reviewed
     * @return list of feedback
     */
    List<Feedback> findAllByOrder(Order order);
}
