package com.danven.web_library.domain.product;

import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a drink product with alcohol and carbonation properties.
 */
@Entity
@Table(name = "DRINK")
public class Drink extends Product {

    @Min(value = 0, message = "Alcohol percentage must be non-negative.")
    @Column(name = "alcohol", nullable = false)
    private double alcohol;

    @Column(name = "is_carbonated", nullable = false)
    private boolean isCarbonated;

    protected static final double BASE_TIME = 0.5;
    protected static final double ALCOHOLIC_ADD = 1.0;
    protected static final double CARBONATED_ADD = 0.5;

    /**
     * Default constructor for JPA.
     */
    public Drink() {
    }

    /**
     * Constructs a new Drink with all details and assigned categories.
     *
     * @param productName       name of the drink.
     * @param productDescription optional description.
     * @param price             price of the drink.
     * @param calorie           calorie content.
     * @param weightInGrams     weight of the product.
     * @param alcohol           alcohol percentage.
     * @param isCarbonated      whether it is carbonated.
     * @param categories        assigned categories.
     */
    public Drink(String productName, String productDescription, BigDecimal price,
                 int calorie, double weightInGrams, double alcohol, boolean isCarbonated,
                 Set<Category> categories) {
        super(productName, productDescription, price, calorie, weightInGrams, categories);
        this.alcohol = alcohol;
        this.isCarbonated = isCarbonated;
    }

    public double getAlcohol() {
        return alcohol;
    }

    public void setAlcohol(double alcohol) {
        if (alcohol < 0) {
            throw new IllegalArgumentException("Alcohol level cannot be negative.");
        }
        this.alcohol = alcohol;
    }

    public boolean isAlcoholic() {
        return alcohol > 0.0;
    }

    public boolean isCarbonatedDrink() {
        return isCarbonated;
    }

    public void setCarbonated(boolean carbonated) {
        isCarbonated = carbonated;
    }

    /**
     * Calculates estimated preparation time based on alcohol and carbonation.
     *
     * @return time in minutes.
     */
    @Override
    public double calculateEstimatedTimeOfPreparation() {
        double time = BASE_TIME;
        if (isAlcoholic()) time += ALCOHOLIC_ADD;
        if (isCarbonatedDrink()) time += CARBONATED_ADD;
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Drink drink = (Drink) o;
        return id != null && Objects.equals(id, drink.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Drink{" +
                "alcohol=" + alcohol +
                ", isCarbonated=" + isCarbonated +
                "} " + super.toString();
    }
}
