package com.danven.web_library.dto;

import com.danven.web_library.domain.product.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for both creating and editing Product entities.
 *
 * This DTO includes all basic product attributes (id, name, description, price,
 * calorie, weight), the list of category IDs to assign, and comprehensive image
 * management metadata (new uploads, existing images, which to keep or remove,
 * and preview selection).
 *
 * It also carries a required 'type' field indicating the product subtype
 * (FOOD, DRINK, DESSERT, MILK_COCKTAIL), and the corresponding subtype-specific
 * properties: ingredientsRaw for FOOD; alcohol and carbonated for DRINK;
 * sugarPerGram for DESSERT; and the full combination plus iceCreamType for
 * MILK_COCKTAIL.
 *
 * Two convenience factory methods simplify controller code:
 * - fromEntity(Product) converts an existing Product entity into a fully
 *   populated DTO for editing.
 * - forCreate() produces an empty DTO preconfigured with default values and
 *   empty lists for the "add new product" form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEditDto {

    private Long id;

    @NotBlank
    private String productName;
    private String productDescription;

    @NotNull @DecimalMin("0.0")
    private BigDecimal price;

    @Min(1)
    private int calorie;

    /** Key indicating preview image source: "existing-{id}" or "new-{index}". */
    private String previewImageKey;

    @DecimalMin("1.0")
    private double weightInGrams;

    @NotEmpty
    private List<@NotNull Long> categoryIds = new ArrayList<>();

    /** Files uploaded in this request. */
    private List<MultipartFile> newImages = new ArrayList<>();

    /** Zero-based index in newImages to use as the preview. */
    private Integer previewNewImageIndex = 0;


    /** DTOs for images already stored in the database. */
    private List<ImageDto> existingImages = new ArrayList<>();

    /** ID of the existing image chosen as preview. */
    private Long previewExistingImageId;

    /** IDs of existing images marked for removal. */
    private List<@NotNull Long> removeImageIds = new ArrayList<>();

    /** IDs of existing images to keep. */
    private List<@NotNull Long> keepImageIds = new ArrayList<>();

    @NotNull
    private String type = "FOOD"; // FOOD, DRINK, DESSERT, MILK_COCKTAIL

    @DecimalMin("0.0")
    private Double alcohol;
    private Boolean carbonated;

    @DecimalMin("0.0")
    private Double sugarPerGram;

    private IceCreamType iceCreamType;
    /** Comma-separated ingredients for FOOD subtype. */
    private String ingredientsRaw;

     /**
     * Creates a ProductEditDto prefilled from an existing Product entity.
     * Copies basic fields, category IDs, maps existing images (including preview
     * flags), and fills subtype-specific properties based on the entity type.
     *
     * @param p the Product entity to convert; must not be null
     * @return a fully populated DTO for editing
     * @throws IllegalArgumentException if p is null
     */
    public static ProductEditDto fromEntity(Product p) {
        if (p == null) {
            throw new IllegalArgumentException("Невозможно преобразовать null Product в ProductEditDto");
        }

        ProductEditDto dto = new ProductEditDto();
        dto.setId(p.getId());
        dto.setProductName(p.getProductName());
        dto.setProductDescription(p.getProductDescription());
        dto.setPrice(p.getPrice());
        dto.setCalorie(p.getCalorie());
        dto.setWeightInGrams(p.getWeightInGrams());

        dto.setCategoryIds(
                p.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toList())
        );

        if (p instanceof MilkCocktail mc) {
            dto.setType("MILK_COCKTAIL");
            dto.setAlcohol(Optional.ofNullable(mc.getAlcohol()).orElse(0.0));
            dto.setCarbonated(Optional.ofNullable(mc.isCarbonatedDrink()).orElse(false));
            dto.setSugarPerGram(Optional.ofNullable(mc.getSugarAmountGramProduct()).orElse(0.0));
            dto.setIceCreamType(mc.getIceCreamType());
        } else if (p instanceof Dessert ds) {
            dto.setType("DESSERT");
            dto.setSugarPerGram(Optional.ofNullable(ds.getSugarAmountGramProduct()).orElse(0.0));
        } else if (p instanceof Food f) {
            dto.setType("FOOD");
            dto.setIngredientsRaw(String.join(",", f.getIngredients()));
        } else if (p instanceof Drink d) {
            dto.setType("DRINK");
            dto.setAlcohol(Optional.ofNullable(d.getAlcohol()).orElse(0.0));
            dto.setCarbonated(Optional.ofNullable(d.isCarbonatedDrink()).orElse(false));
        }

        List<ImageDto> images = p.getImages().stream()
                .map(img -> {
                    String base64 = Base64.getEncoder().encodeToString(img.getImage());
                    return new ImageDto(img.getId(), img.isPreview(), img.getFormat().name(), base64);
                })
                .collect(Collectors.toList());
        dto.setExistingImages(images);

        List<Long> allIds = images.stream()
                .map(ImageDto::getId)
                .collect(Collectors.toList());
        dto.setKeepImageIds(new ArrayList<>(allIds));


        Long previewId = p.getImages().stream()
                .filter(Image::isPreview)
                .map(Image::getId)
                .findFirst()
                .orElse(allIds.isEmpty() ? null : allIds.get(0));
        dto.setPreviewExistingImageId(previewId);


        dto.setNewImages(new ArrayList<>());
        dto.setPreviewNewImageIndex(0);


        dto.setRemoveImageIds(new ArrayList<>());

        return dto;
    }


    /**
     * Creates an empty ProductEditDto configured for creation.
     * Sets default values and initializes all lists to empty,
     * ready for the "add new product" form.
     *
     * @return a DTO with defaults for creating a product
     */
    public static ProductEditDto forCreate() {
        ProductEditDto dto = new ProductEditDto();
        dto.setType("FOOD");
        dto.setPrice(BigDecimal.ZERO);
        dto.setCalorie(0);
        dto.setWeightInGrams(0.0);
        dto.setCategoryIds(new ArrayList<>());
        dto.setExistingImages(Collections.emptyList());
        dto.setPreviewExistingImageId(null);
        dto.setRemoveImageIds(new ArrayList<>());
        dto.setKeepImageIds(new ArrayList<>());
        dto.setNewImages(new ArrayList<>());
        dto.setPreviewNewImageIndex(0);
        return dto;
    }
}
