
package com.danven.web_library.service;

import com.danven.web_library.domain.feedback.Feedback;
import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


/**
 * Service implementation for managing customer feedback.
 * Handles creation of Feedback entries and retrieval of feedback
 * associated with specific orders.
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository    feedbackRepo;
    private final OrderPersistenceService orderService;

    public FeedbackServiceImpl(FeedbackRepository feedbackRepo,
                               OrderPersistenceService orderService) {
        this.feedbackRepo = feedbackRepo;
        this.orderService = orderService;
    }

    /**
     * Creates and persists a new {@link Feedback} for the given order.
     * Associates the feedback with the order’s customer and stamps it
     * with the current timestamp.
     *
     * @param orderId the ID of the order to attach feedback to
     * @param text    the feedback text
     * @return the saved {@link Feedback} instance
     */
    @Override
    public Feedback leaveFeedback(Long orderId, String text) {

        Order order = orderService.findById(orderId);

        Customer customer = order.getCustomer();

        Feedback fb = new Feedback(text, LocalDateTime.now(), order, customer);
        return feedbackRepo.save(fb);
    }

    /**
     * Retrieves all feedback entries associated with the specified order.
     *
     * @param orderId the ID of the order
     * @return a list of {@link Feedback} for that order
     */
    @Override
    public List<Feedback> getByOrder(Long orderId) {
        Order order = orderService.findById(orderId);
        return feedbackRepo.findAllByOrder(order);
    }

    /**
     * Checks whether any feedback exists for the given order.
     *
     * @param orderId the ID of the order
     * @return true if at least one feedback entry exists, false otherwise
     */
    @Override
    public boolean hasFeedbacks(Long orderId) {
        return !getByOrder(orderId).isEmpty();
    }
}
