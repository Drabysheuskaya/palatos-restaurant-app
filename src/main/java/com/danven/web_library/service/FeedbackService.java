// src/main/java/com/darina/PalaTOS/service/FeedbackService.java
package com.danven.web_library.service;

import com.danven.web_library.domain.feedback.Feedback;

import java.util.List;

public interface FeedbackService {

    /**
     * Создать новый отзыв к заказу.
     *
     * @param orderId идентификатор заказа
     * @param text    текст отзыва
     * @return сохранённый объект Feedback
     */
    Feedback leaveFeedback(Long orderId, String text);

    /**
     * Получить все отзывы по конкретному заказу.
     *
     * @param orderId идентификатор заказа
     * @return список отзывов
     */
    List<Feedback> getByOrder(Long orderId);

    /**
     * Проверить, есть ли отзывы у данного заказа.
     *
     * @param orderId идентификатор заказа
     * @return true, если хотя бы один отзыв есть
     */
    boolean hasFeedbacks(Long orderId);
}
