package com.danven.web_library.repository;

import com.danven.web_library.domain.product.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Repository interface for accessing {@link ProductOrder} entities.
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for retrieving order line items by order or product.
 */
@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    /**
     * Retrieves all product orders linked to a specific order.
     *
     * @param orderId the ID of the order
     * @return list of product orders for that order
     */
    List<ProductOrder> findByOrderId(Long orderId);

    /**
     * Retrieves all product orders for a specific product.
     *
     * @param productId the ID of the product
     * @return list of product orders that contain the product
     */
    List<ProductOrder> findByProductId(Long productId);
}
