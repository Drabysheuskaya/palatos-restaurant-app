package com.danven.web_library.domain.product;

import javax.persistence.*;
import javax.validation.ValidationException;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a milk-based cocktail that extends Drink and implements IDessert.
 * Supports hybrid categorization (e.g., Drink + Dessert).
 */
@Entity
@Table(name = "MILK_COCKTAIL")
public class MilkCocktail extends Drink implements IDessert {

    /**
     * Type of ice cream used in the cocktail. Cannot be null.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ice_cream_type", nullable = false)
    private IceCreamType iceCreamType;

    /**
     * Sugar amount per gram of product in grams. Must be non-negative.
     */
    @Min(value = 0, message = "Sugar amount must be non-negative.")
    @Column(name = "sugar_amount_gram_product", nullable = false)
    private double sugarAmountGramProduct;

    private static final double BASE_TIME = 5.0;
    private static final double TIME_PER_GRAM_SUGAR = 0.2;

    /**
     * Default constructor for JPA.
     */
    public MilkCocktail() {
    }

    /**
     * Constructs a new MilkCocktail with all required parameters.
     *
     * @param productName            the name of the milk cocktail
     * @param productDescription     optional description
     * @param price                  price of the product
     * @param calorie                calorie count
     * @param weightInGrams          weight of the cocktail in grams
     * @param alcohol                alcohol percentage
     * @param isCarbonated           whether it is carbonated
     * @param iceCreamType           type of ice cream used
     * @param sugarAmountGramProduct sugar per gram of product
     * @param categories             categories it belongs to (Drink/Dessert)
     */
    public MilkCocktail(String productName, String productDescription, BigDecimal price,
                        int calorie, double weightInGrams, double alcohol, boolean isCarbonated,
                       IceCreamType iceCreamType, double sugarAmountGramProduct,
                        Set<Category> categories) {
        super(productName, productDescription, price, calorie, weightInGrams, alcohol, isCarbonated, categories);
        setIceCreamType(iceCreamType);
        setSugarAmountGramProduct(sugarAmountGramProduct);
    }

    /**
     * Returns the type of ice cream used in the cocktail.
     *
     * @return the {@link IceCreamType}
     */
    public IceCreamType getIceCreamType() {
        return iceCreamType;
    }


    /**
     * Sets the type of ice cream for this cocktail.
     *
     * @param iceCreamType the ice cream type; must not be null
     * @throws ValidationException if {@code iceCreamType} is null
     */
    public void setIceCreamType(IceCreamType iceCreamType) {
        if (iceCreamType == null) {
            throw new ValidationException("Ice cream type must not be null.");
        }
        this.iceCreamType = iceCreamType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getSugarAmountGramProduct() {
        return sugarAmountGramProduct;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSugarAmountGramProduct(double sugarAmountGramProduct) {
        if (sugarAmountGramProduct < 0) {
            throw new ValidationException("Sugar amount per gram must be non-negative.");
        }
        this.sugarAmountGramProduct = sugarAmountGramProduct;
    }

    /**
     * {@inheritDoc}
     * Calculates the total sugar based on per-gram value and overall weight.
     *
     * @return total sugar in grams
     */
    @Override
    public double calculateTotalAmountOfSugar() {
        return sugarAmountGramProduct * weightInGrams;
    }

    /**
     * {@inheritDoc}
     * Estimated preparation time is base time plus additional time per gram of sugar.
     *
     * @return estimated preparation time in minutes
     */
    @Override
    public double calculateEstimatedTimeOfPreparation() {
        return BASE_TIME + (calculateTotalAmountOfSugar() * TIME_PER_GRAM_SUGAR);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MilkCocktail)) return false;
        if (!super.equals(o)) return false;
        MilkCocktail that = (MilkCocktail) o;
        return Double.compare(that.sugarAmountGramProduct, sugarAmountGramProduct) == 0 &&
                iceCreamType == that.iceCreamType;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), iceCreamType, sugarAmountGramProduct);
    }


    /**
     * {@inheritDoc}
     * Includes iceCreamType and sugar amount details.
     */
    @Override
    public String toString() {
        return "MilkCocktail{" +
                "iceCreamType=" + iceCreamType +
                ", sugarAmountGramProduct=" + sugarAmountGramProduct +
                "} " + super.toString();
    }
}
