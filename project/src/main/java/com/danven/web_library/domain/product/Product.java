package com.danven.web_library.domain.product;


import com.danven.web_library.exceptions.ValidationException;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Abstract base class representing a product in the system.
 * Implements joined inheritance strategy to allow specialized product types.
 * Contains common fields like name, description, price, calorie, and weight.
 * Also manages relationships to categories, images, and order lines.
 *
 * Named Entity Graphs:
 * Two graphs are defined to optimize JPA fetch strategies:
 * product-with-categories-and-images:
 *       Fetches both {@link #categories} and {@link #images} when loading a Product.
 *       Useful for use cases where full product details (including categories and images)
 *       must be displayed in a single transaction.
 * product-with-images:
 *       Fetches only {@link #images} when loading a Product.
 *       Applicable when categories are not needed, reducing join overhead.
 * </p>
 */
@NamedEntityGraphs({
        @NamedEntityGraph(
                name = "product-with-categories-and-images",
                attributeNodes = {
                        @NamedAttributeNode("categories"),
                        @NamedAttributeNode("images")
                }
        ),
        @NamedEntityGraph(
                name = "product-with-images",
                attributeNodes = @NamedAttributeNode("images")
        )
})
@Entity
@Table(name = "PRODUCT")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Product implements Serializable {

    /**
     * Primary key for Product.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    protected Long id;

    /**
     * Name of the product. Cannot be blank.
     */
    @NotBlank(message = "Product name must not be blank.")
    @Column(name = "product_name", nullable = false)
    protected String productName;

    /**
     * Optional description of the product.
     */
    @Column(name = "product_description")
    protected String productDescription;

    /**
     * Base price of the product. Must be non-negative.
     */
    @Min(value = 0, message = "Price must be non-negative.")
    @Column(name = "price", nullable = false)
    protected BigDecimal price;

    /**
     * Caloric value. Minimum value of 1 kcal.
     */
    @Min(value = 1, message = "Calorie must be at least 1.")
    @Column(name = "calorie", nullable = false)
    protected int calorie;

    /**
     * Weight in grams. Minimum value of 1 gram.
     */
    @Min(value = 1, message = "Weight must be at least 1 gram.")
    @Column(name = "weight_grams", nullable = false)
    protected double weightInGrams;

    /**
     * Categories assigned to this product. Bidirectional many-to-many.
     */
    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = { CascadeType.PERSIST, CascadeType.MERGE }
    )
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )

    protected Set<Category> categories = new HashSet<>();

    /**
     * Images associated with this product. Bidirectional one-to-many.
     * Orphan removal ensures images are deleted when no longer linked.
     */
    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    protected Set<Image> images = new HashSet<>();

    /**
     * Order lines referencing this product. Represents historical pricing and quantity.
     */
    @OneToMany(
            mappedBy = "product",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    protected Set<ProductOrder> productOrders = new HashSet<>();

    /**
     * Default constructor for JPA.
     */
    public Product() {}


    /**
     * Constructs a new product with specified details and categories.
     *
     * @param productName        non-blank product name
     * @param productDescription optional description
     * @param price              non-negative base price
     * @param calorie            at least 1 kcal
     * @param weightInGrams      at least 1 gram
     * @param categories         initial categories, can be null or empty
     */
    public Product(String productName,
                   String productDescription,
                   BigDecimal price,
                   int calorie,
                   double weightInGrams,
                   Set<Category> categories)
    {
        this.productName = productName;
        this.productDescription = productDescription;
        this.price = price;
        this.calorie = calorie;
        this.weightInGrams = weightInGrams;
        setCategories(categories);
    }

    /**
     * Calculate an estimated preparation time for this product.
     * Subclasses must provide their own implementation.
     *
     * @return estimated time in minutes
     */
    public abstract double calculateEstimatedTimeOfPreparation();

    /**
     * Adds a category to this product and ensures bidirectional sync.
     *
     * @param c category to add; ignored if null or already present
     */
    public void addCategory(Category c) {
        if (c != null && !categories.contains(c)) {
            categories.add(c);
            if (!c.getProducts().contains(this)) {
                c.addProduct(this);
            }
        }
    }

    /**
     * Removes a category from this product and ensures bidirectional sync.
     *
     * @param c category to remove; ignored if null or not associated
     */
    public void removeCategory(Category c) {
        if (c != null && categories.remove(c)) {
            if (c.getProducts().contains(this)) {
                c.removeProduct(this);
            }
        }
    }


    /**
     * Replaces current categories with the provided set.
     * Existing links are cleared before adding new ones.
     *
     * @param cats new categories; if null, clears all
     */
    public void setCategories(Set<Category> cats) {
        // unlink old
        for (Category old : new HashSet<>(categories)) {
            removeCategory(old);
        }
        // link new
        if (cats != null) {
            cats.forEach(this::addCategory);
        }
    }

    /**
     * Returns an immutable copy of associated categories.
     *
     * @return set of categories
     */
    public Set<Category> getCategories() {
        return new HashSet<>(categories);
    }


    /**
     * Adds an image to this product, validating linkage and setting reverse reference.
     *
     * @param image image to add; must not be null or linked elsewhere
     * @throws ValidationException on invalid image
     */
    public void addImage(Image image) {
        validateImage(image);
        images.add(image);
        image.setProduct(this);
    }

    /**
     * Removes an image from this product and clears its product reference.
     *
     * @param img image to remove; ignored if null or not associated
     */
    public void removeImage(Image img) {
        if (images.remove(img)) {
            img.setProduct(null);
        }
    }


    /**
     * Returns an immutable copy of associated images.
     *
     * @return set of images
     */
    public Set<Image> getImages() {
        return new HashSet<>(images);
    }

    /**
     * Replaces current images with the provided set, clearing old ones first.
     *
     * @param imgs new images; if null, clears all
     */
    public void setImages(Set<Image> imgs) {
        // unlink all current images
        for (Image old : new HashSet<>(this.images)) {
            removeImage(old);
        }
        // link in the new ones
        if (imgs != null) {
            imgs.forEach(this::addImage);
        }
    }

    /**
     * Validates that the image is not null and not linked to another product.
     *
     * @param image image to validate
     * @throws ValidationException on invalid image
     */
    private void validateImage(Image image) {
        if (image == null) {
            throw new ValidationException("Image must not be null");
        }
        if (image.getProduct() != null && image.getProduct() != this) {
            throw new ValidationException("Image is attached to another product");
        }
    }

    /**
     * Returns all historical order lines for this product.
     * May include different unit prices over time.
     *
     * @return set of {@link ProductOrder}
     */
    public Set<ProductOrder> getProductOrders() {
        return productOrders;
    }

    /**
     * Adds an order line reference for this product.
     * Used internally by {@link ProductOrder}.
     *
     * @param productOrder link to add; ignored if null
     */
    public void addProductOrder(ProductOrder productOrder) {
        if (productOrder != null) {
            productOrders.add(productOrder);
        }
    }

    /**
     * Removes an order line reference for this product.
     * Used internally by {@link ProductOrder}.
     *
     * @param productOrder link to remove; ignored if null
     */
    public void removeProductOrder(ProductOrder productOrder) {
        if (productOrder != null) {
            productOrders.remove(productOrder);
        }
    }

    /**
     * @return the database-generated identifier
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the product's name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Updates the product's name. Must not be blank.
     *
     * @param productName new name
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * @return the product description, may be null
     */
    public String getProductDescription() {
        return productDescription;
    }

    /**
     * Updates the product's description.
     *
     * @param productDescription new description
     */
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    /**
     * @return the base price of the product
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Updates the base price. Must be non-negative.
     *
     * @param price new price
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * @return caloric value of the product
     */
    public int getCalorie() {
        return calorie;
    }

    /**
     * Updates the caloric value. Must be at least 1.
     *
     * @param calorie new calorie count
     */
    public void setCalorie(int calorie) {
        this.calorie = calorie;
    }

    /**
     * @return weight of the product in grams
     */
    public double getWeightInGrams() {
        return weightInGrams;
    }

    /**
     * Updates the weight in grams. Must be at least 1 gram.
     *
     * @param weightInGrams new weight
     */
    public void setWeightInGrams(double weightInGrams) {
        this.weightInGrams = weightInGrams;
    }

    /**
     * Equality based on the database identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Product that = (Product) o;
        return id != null && Objects.equals(id, that.id);
    }


    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
