package com.danven.web_library.dto;

import com.danven.web_library.domain.product.IceCreamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for Product entities.
 * Encapsulates all common product fields, relationships, and subtype-specific attributes
 * for use in views and API responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {


    /** Unique identifier of the product. */
    private Long id;
    /** Display name of the product. */
    private String productName;
    /** Detailed description of the product; may be null or empty. */
    private String productDescription;
    /** Base price of the product. */
    private BigDecimal price;
    /** Caloric value of the product (in kcal). */
    private int calorie;
    /** Weight of the product (in grams). */
    private double weightInGrams;
    /** Weight of the product (in grams). */
    private List<CategoryDto> categories;
    /** String identifier for the product’s subtype (e.g., "Food", "Drink"). */
    private List<ImageDto> images;

    // Subtype-specific fields:

    /** Alcohol percentage (for drinks); null if not applicable. */
    private String type;

    /** Alcohol percentage (for drinks); null if not applicable. */
    private Double alcohol;
    /** True if carbonated (for drinks); null if not applicable. */
    private Boolean carbonated;
    /** Sugar amount per gram (for desserts/milk cocktails); null if not applicable. */
    private Double sugarPerGram;
    /** Type of ice cream used (for milk cocktails); null if not applicable. */
    private IceCreamType iceCreamType;
    /** List of ingredients (for food products); null or empty if not applicable. */
    private List<String> ingredients;
}
