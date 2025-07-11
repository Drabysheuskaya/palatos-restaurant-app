// src/main/java/com/darina/PalaTOS/repository/ProductRepository.java
package com.danven.web_library.repository;

import com.danven.web_library.domain.product.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing {@link Product} entities.
 * Extends JpaRepository to provide CRUD operations and custom queries
 * that eagerly fetch associated categories and images without using EntityGraph.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Retrieves all products, eagerly loading their categories and images
     * via the {@code product-with-categories-and-images} graph.
     *
     * @return list of all products with categories and images fetched
     */
    @EntityGraph(value = "product-with-categories-and-images", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithCategoriesAndImages();

    /**
     * Retrieves all products in the given category (by ID), eagerly loading
     * categories and images via the {@code product-with-categories-and-images} graph.
     *
     * @param categoryId the ID of the category to filter by
     * @return list of products in that category with categories and images fetched
     */
    @EntityGraph(value = "product-with-categories-and-images", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * Find a single product by ID, fetching categories and images.
     */
    @EntityGraph(value = "product-with-categories-and-images", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithCategoriesAndImages(@Param("id") Long id);

    /**
     * An alternative “all” fetch using the same graph but without a custom JPQL.
     */
    @EntityGraph(attributePaths = {"images", "categories"}, type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Product p")
    List<Product> findAllProductsWithCategoriesAndImages();

    /**
     * Find by category name with categories and images loaded.
     */
    @EntityGraph(value = "product-with-categories-and-images", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.name = :categoryName")
    List<Product> findByCategoryName(@Param("categoryName") String categoryName);
}
