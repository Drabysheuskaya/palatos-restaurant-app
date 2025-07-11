package com.danven.web_library.service;


import com.danven.web_library.domain.product.Category;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for interacting with book categories.
 */
public interface CategoryService {

    /**
     * Retrieve all categories.
     * @return list of all Category entities.
     */
    List<Category> getAllCategories();

    /**
     * Find a category by its unique name.
     * @param name the category name
     * @return an Optional containing the Category if found
     */
    Optional<Category> getCategoryByName(String name);

    /**
     * Check whether a category with the given name exists.
     * @param name the category name
     * @return true if a category with that name exists
     */
    boolean categoryExists(String name);
}
