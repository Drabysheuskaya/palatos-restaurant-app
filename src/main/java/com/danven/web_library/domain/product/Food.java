package com.danven.web_library.domain.product;

import com.danven.web_library.exceptions.ValidationException;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "FOOD")
public class Food extends Product {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "food_ingredients", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "ingredient", nullable = false)
    @NotEmpty(message = "Food must have at least one ingredient.")
    private List<String> ingredients = new ArrayList<>();

    private static final double BASE_TIME = 5.0;
    private static final double TIME_PER_INGREDIENT = 4.0;
    private static final double SAUCE_COMPLEXITY = 1.0;

    public Food() {}

    public Food(String productName, String productDescription, BigDecimal price,
                int calorie, double weightInGrams, Set<Category> categories,
                List<String> ingredients) {
        super(productName, productDescription, price, calorie, weightInGrams, categories);
        setIngredients(ingredients);
    }

    public List<String> getIngredients() {
        return new ArrayList<>(ingredients);
    }

    public void setIngredients(List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            throw new ValidationException("Ingredients list cannot be null or empty.");
        }
        this.ingredients.clear();
        this.ingredients.addAll(ingredients);
    }

    public boolean hasIngredient(String ingredient) {
        return ingredients.stream()
                .anyMatch(i -> i.equalsIgnoreCase(ingredient));
    }

    public boolean isVegetarian() {
        List<String> nonVegetarian = List.of("meat", "chicken", "fish", "bacon", "ham", "pork", "beef");
        return ingredients.stream()
                .noneMatch(i -> nonVegetarian.stream()
                        .anyMatch(nonVeg -> i.equalsIgnoreCase(nonVeg)));
    }

    @Override
    public double calculateEstimatedTimeOfPreparation() {
        double time = BASE_TIME + TIME_PER_INGREDIENT * ingredients.size();
        if (hasIngredient("sauce")) {
            time += SAUCE_COMPLEXITY;
        }
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Food)) return false;
        if (!super.equals(o)) return false;
        Food food = (Food) o;
        return Objects.equals(ingredients, food.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ingredients);
    }

    @Override
    public String toString() {
        return "Food{" +
                "ingredients=" + ingredients +
                "} " + super.toString();
    }
}
