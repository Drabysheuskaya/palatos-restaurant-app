// src/main/java/com/darina/PalaTOS/repository/CategoryRepository.java
package com.danven.web_library.repository;

import com.danven.web_library.domain.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on Category entities.
 * Extends JpaRepository to provide basic data access methods.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Retrieves a category by its exact name.
     *
     * @param name the category name
     * @return an Optional containing the category if found
     */
    @Query("SELECT c FROM Category c WHERE c.name = :name")
    Optional<Category> findByName(@Param("name") String name);

    /**
     * Checks if a category exists by name.
     *
     * @param name the category name
     * @return true if a matching category exists
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name")
    boolean existsByName(@Param("name") String name);
}
