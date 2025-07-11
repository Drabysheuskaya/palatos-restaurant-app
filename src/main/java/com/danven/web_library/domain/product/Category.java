package com.danven.web_library.domain.product;

import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a category of products, allowing products to be grouped under named labels.
 * Categories can be assigned to multiple {@link Product} entities (many-to-many).
 * Provides methods to manage the bidirectional relationship and ensures data integrity.
 */


@NamedEntityGraph(
        name = "category-with-products",
        attributeNodes = @NamedAttributeNode("products")
)
@Entity
@Table(name = "CATEGORY")
public class Category implements Serializable {

    /**
     * Primary key for the Category entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    /**
     * Unique name of the category. Cannot be blank.
     */
    @NotBlank(message = "Category name must not be blank.")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Products assigned to this category. Bidirectional many-to-many.
     */
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    /**
     * Default constructor for Category.
     */
    protected Category() {
    }

    /**
     * Constructs a new Category with the given name.
     *
     * @param name non-blank unique name of the category
     * @throws IllegalArgumentException if name is null or blank
     */
    public Category(String name) {
        setName(name);
    }


    /**
     * Adds a product to this category, maintaining bidirectional sync.
     *
     * @param product the product to add; ignored if null or already present
     */
    public void addProduct(Product product) {
        if (product != null && !this.products.contains(product)) {
            this.products.add(product);
            if (!product.getCategories().contains(this)) {
                product.addCategory(this);
            }
        }
    }

    /**
     * Removes a product from this category, maintaining bidirectional sync.
     *
     * @param product the product to remove; ignored if null or not present
     */
    public void removeProduct(Product product) {
        if (product != null && this.products.remove(product)) {
            if (product.getCategories().contains(this)) {
                product.removeCategory(this);
            }
        }
    }

    /**
     * @return the database-generated identifier for this category
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the name of the category
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the category. Must be non-null and non-blank.
     *
     * @param name new name for the category
     * @throws IllegalArgumentException if name is null or blank
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name must not be blank");
        }
        this.name = name.trim();
    }


    /**
     * @return an immutable copy of products in this category
     */
    public Set<Product> getProducts() {
        return new HashSet<>(products);
    }

    /**
     * Replaces current product assignments with the provided set,
     * clearing old links and adding new ones with bidirectional sync.
     *
     * @param prods new set of products; if null, clears all links
     */
    public void setProducts(Set<Product> prods) {
        // unlink old
        for (Product old : new HashSet<>(products)) {
            removeProduct(old);
        }
        // link new
        if (prods != null) {
            prods.forEach(this::addProduct);
        }
    }

    /**
     * Equality is based on database identifier and class type.
     *
     * @param o other object to compare
     * @return true if entities have the same id
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Category that = (Category) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
