package com.danven.web_library.controller;

import com.danven.web_library.config.CustomUserDetailsService;
import com.danven.web_library.domain.user.Employee;
import com.danven.web_library.domain.user.User;
import com.danven.web_library.dto.ImageDto;
import com.danven.web_library.dto.ProductDto;
import com.danven.web_library.service.CategoryService;
import com.danven.web_library.service.ImageService;
import com.danven.web_library.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller responsible for displaying the product menu and details.
 * Supports listing categories, fetching products and images via AJAX,
 * and rendering the product details page.
 */
@Controller
@RequestMapping("/menu")
public class MenuController {
    private final CategoryService          categoryService;
    private final ProductService           productService;
    private final ImageService             imageService;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Constructs a MenuController with all required services.
     *
     * @param categoryService      service for retrieving product categories
     * @param productService       service for retrieving product data
     * @param imageService         service for retrieving product images
     * @param userDetailsService   service for obtaining the current user
     */
    public MenuController(CategoryService categoryService,
                          ProductService productService,
                          ImageService imageService,
                          CustomUserDetailsService userDetailsService) {
        this.categoryService    = categoryService;
        this.productService     = productService;
        this.imageService       = imageService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Displays the main menu page with the list of categories
     * and current user information.
     *
     * @param model the Spring MVC model for view attributes
     * @return the view name "menu"
     */
    @GetMapping
    public String menuPage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("user",       userDetailsService.getLoggedInUser());
        model.addAttribute("isEmployee", userDetailsService.getLoggedInUser() instanceof Employee);
        return "menu";
    }

    /**
     * Returns a list of products in the given category as JSON.
     * If categoryId is 0, returns all products.
     *
     * @param categoryId the ID of the category to filter by, or 0 for all
     * @return list of {@link ProductDto}
     */
    @GetMapping("/products")
    @ResponseBody
    public List<ProductDto> productsByCategory(@RequestParam("categoryId") Long categoryId) {
        return categoryId == 0
                ? productService.getAllProducts()
                : productService.getProductsByCategory(categoryId);
    }

    /**
     * Returns all images for the specified product as JSON.
     *
     * @param productId the ID of the product
     * @return list of {@link ImageDto}
     */
    @GetMapping("/images")
    @ResponseBody
    public List<ImageDto> imagesByProduct(@RequestParam("productId") Long productId) {
        return imageService.findAllByProduct(productId);
    }

    /**
     * Renders the product details page for a single product.
     * Adds product DTO, preview image URI, inferred product type,
     * and user context to the model.
     *
     * @param id    the ID of the product to view
     * @param model the Spring MVC model for view attributes
     * @return the view name "product-details"
     */
    @GetMapping("/details/{id}")
    public String showProductDetails(@PathVariable Long id, Model model) {
        // 1) load product DTO
        ProductDto product = productService.getProductById(id);
        model.addAttribute("product", product);

        // 2) select preview image and build data URI
        List<ImageDto> images = imageService.findAllByProduct(id);
        ImageDto chosen = images.stream()
                .filter(ImageDto::isPreview)
                .findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));

        String previewDataUri = "";
        if (chosen != null
                && chosen.getBase64Data() != null
                && !chosen.getBase64Data().isBlank()) {
            previewDataUri = "data:image/"
                    + chosen.getFormat().toLowerCase()
                    + ";base64,"
                    + chosen.getBase64Data();
        }
        model.addAttribute("previewImage", previewDataUri);

        // 3) infer product type for UI
        String type;
        if (product.getIngredients() != null) {
            type = "Food";
        } else if (product.getSugarPerGram() != null && product.getIceCreamType() != null) {
            type = "MilkCocktail";
        } else if (product.getSugarPerGram() != null) {
            type = "Dessert";
        } else {
            type = "Drink";
        }
        model.addAttribute("productType", type);

        // 4) add user context
        User user = userDetailsService.getLoggedInUser();
        model.addAttribute("user", user);
        model.addAttribute("isEmployee", user instanceof Employee);

        return "product-details";
    }

}
