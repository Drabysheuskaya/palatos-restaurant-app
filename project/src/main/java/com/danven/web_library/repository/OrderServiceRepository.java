package com.danven.web_library.repository;

import com.danven.web_library.domain.order.OrderService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing OrderService entities (including subclasses like RegularOrderService and HolidayOrderService).
 */
@Repository
public interface OrderServiceRepository extends JpaRepository<OrderService, Long> {
}
