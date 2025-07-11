// src/main/java/com/darina/PalaTOS/service/CategoryServiceImpl.java
package com.danven.web_library.service;

import com.danven.web_library.domain.product.Category;
import com.danven.web_library.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Default implementation of CategoryService.
 * Handles category retrieval and lookup logic.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean categoryExists(String name) {
        return categoryRepository.existsByName(name);
    }
}
