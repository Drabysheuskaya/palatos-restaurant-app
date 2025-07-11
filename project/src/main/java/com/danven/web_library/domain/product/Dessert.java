package com.danven.web_library.domain.product;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a dessert product with sugar-related logic.
 */
@Entity
@Table(name = "DESSERT")
public class Dessert extends Product implements IDessert {

    @Min(value = 0, message = "Sugar amount must be non-negative.")
    @Column(name = "sugar_amount_per_gram_product", nullable = false)
    private double sugarAmountGramProduct;

    private static final double BASE_TIME = 5.0;
    private static final double TIME_PER_GRAM_SUGAR = 0.01;

    /**
     * Default constructor for JPA.
     */
    public Dessert() {
    }

    /**
     * Constructs a Dessert with full fields and categories.
     *
     * @param productName           name of the dessert.
     * @param productDescription    optional description.
     * @param price                 price of the dessert.
     * @param calorie               calorie content.
     * @param weightInGrams         weight in grams.
     * @param sugarAmountGramProduct amount of sugar per gram of product.
     * @param categories            assigned categories.
     */
    public Dessert(String productName, String productDescription, BigDecimal price,
                   int calorie, double weightInGrams, double sugarAmountGramProduct,
                   Set<Category> categories) {
        super(productName, productDescription, price, calorie, weightInGrams, categories);
        setSugarAmountGramProduct(sugarAmountGramProduct);
    }

    /**
     * Gets sugar per gram.
     */
    @Override
    public double getSugarAmountGramProduct() {
        return sugarAmountGramProduct;
    }

    /**
     * Sets sugar per gram with validation.
     */
    @Override
    public void setSugarAmountGramProduct(double sugarAmountGramProduct) {
        if (sugarAmountGramProduct < 0) {
            throw new IllegalArgumentException("Sugar amount per gram must be non-negative.");
        }
        this.sugarAmountGramProduct = sugarAmountGramProduct;
    }

    /**
     * Calculates total sugar based on weight.
     */
    @Override
    public double calculateTotalAmountOfSugar() {
        return sugarAmountGramProduct * weightInGrams;
    }

    /**
     * Calculates preparation time.
     */
    @Override
    public double calculateEstimatedTimeOfPreparation() {
        return BASE_TIME + (sugarAmountGramProduct * weightInGrams * TIME_PER_GRAM_SUGAR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dessert)) return false;
        if (!super.equals(o)) return false;
        Dessert dessert = (Dessert) o;
        return Double.compare(dessert.sugarAmountGramProduct, sugarAmountGramProduct) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sugarAmountGramProduct);
    }

    @Override
    public String toString() {
        return "Dessert{" +
                "sugarAmountGramProduct=" + sugarAmountGramProduct +
                "} " + super.toString();
    }
}
