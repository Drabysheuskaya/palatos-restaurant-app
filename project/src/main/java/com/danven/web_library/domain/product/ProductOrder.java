package com.danven.web_library.domain.product;

import com.danven.web_library.domain.order.Order;
import com.danven.web_library.exceptions.ValidationException;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents the association between a {@link Product} and an {@link Order},
 * capturing the quantity of the product and its price at the time the order was placed.
 */
@Entity
@Table(name = "PRODUCT_ORDER", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "order_id"})
})
public class ProductOrder implements Serializable {

    /**
     * Primary key for the ProductOrder entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_order_id")
    private Long id;

    /**
     * Quantity of the product in this order. Must be greater than zero.
     */
    @Column(name = "amount", nullable = false)
    private int amount;

    /**
     * Price per unit of the product at the time the order was created. Must be non-negative.
     */
    @Column(name = "product_order_price", nullable = false)
    private BigDecimal productOrderPrice;

    /**
     * Reference to the associated product. Cannot be null.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    /**
     * Reference to the associated order. Cannot be null.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Order order;

    /**
     * JPA-only no-args constructor.
     */
    protected ProductOrder() {}


    /**
     * Creates a new ProductOrder linking the specified product and order,
     * with the given quantity and unit price.
     *
     * @param product           the product to include in the order; must not be null
     * @param order             the order in which the product is included; must not be null
     * @param amount            the number of product units; must be greater than zero
     * @param productOrderPrice the price per unit at order time; must be non-negative
     * @throws ValidationException if any validation constraint is violated
     */
    public ProductOrder(Product product, Order order, int amount, BigDecimal productOrderPrice) {
        validateProduct(product);
        validateOrder(order);
        validateAmount(amount);
        validateProductOrderPrice(productOrderPrice);

        this.product = product;
        this.order = order;
        this.amount = amount;
        this.productOrderPrice = productOrderPrice;

        this.product.addProductOrder(this);
        this.order.addProductOrder(this);
    }

    /**
     * Removes this link from both the product and the order,
     * effectively disassociating this instance.
     */
    public void removeProductOrder() {
        if (order != null) {
            order.removeProductOrder(this);
        }
        if (product != null) {
            product.removeProductOrder(this);
        }
        this.order = null;
        this.product = null;
    }

    /**
     * Calculates the total price for this line item (unit price multiplied by quantity).
     *
     * @return the subtotal price for this product in the order
     */
    public BigDecimal calculateSubtotalPrice() {
        return productOrderPrice.multiply(BigDecimal.valueOf(amount));
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new ValidationException("Product must not be null.");
        }
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new ValidationException("Order must not be null.");
        }
    }

    private void validateAmount(int amount) {
        if (amount <= 0) {
            throw new ValidationException("Amount must be greater than zero.");
        }
    }

    private void validateProductOrderPrice(BigDecimal productOrderPrice) {
        if (productOrderPrice == null || productOrderPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("ProductOrderPrice must be non-negative.");
        }
    }

    /**
     * @return the database-generated identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the quantity of the product in this order
     */
    public int getAmount() {
        return amount;
    }

    /**
     * @return the price per unit at the time of order creation
     */
    public BigDecimal getProductOrderPrice() {
        return productOrderPrice;
    }

    /**
     * @return the associated product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @return the associated order
     */
    public Order getOrder() {
        return order;
    }


    /**
     * Updates the quantity of the product in the order.
     *
     * @param amount the new quantity; must be greater than zero
     * @throws ValidationException if the new amount is invalid
     */
    public void setAmount(int amount) {
        validateAmount(amount);
        this.amount = amount;
    }

    /**
     * Updates the price per unit for this product in the order.
     *
     * @param productOrderPrice the new unit price; must be non-negative
     * @throws ValidationException if the new price is invalid
     */
    public void setProductOrderPrice(BigDecimal productOrderPrice) {
        validateProductOrderPrice(productOrderPrice);
        this.productOrderPrice = productOrderPrice;
    }

    /**
     * Equality based on product and order references (composite key).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductOrder that)) return false;
        return Objects.equals(product, that.product) &&
                Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product, order);
    }

    /**
     * Returns a string representation including id, amount, unit price,
     * and references to product and order.
     */
    @Override
    public String toString() {
        return "ProductOrder{" +
                "id=" + id +
                ", amount=" + amount +
                ", productOrderPrice=" + productOrderPrice +
                ", product=" + product +
                ", order=" + order +
                '}';
    }

}
