package com.danven.web_library.controller;

import com.danven.web_library.config.CustomUserDetailsService;
import com.danven.web_library.domain.product.IceCreamType;
import com.danven.web_library.domain.product.Product;
import com.danven.web_library.domain.user.Employee;
import com.danven.web_library.dto.ImageDto;
import com.danven.web_library.dto.ProductDto;
import com.danven.web_library.dto.ProductEditDto;
import com.danven.web_library.exceptions.ValidationException;
import com.danven.web_library.service.CategoryService;
import com.danven.web_library.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * Controller for creating, editing, viewing, and deleting products.
 * Only users with Employee role are authorized to add, edit, or delete.
 */
@Controller
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CustomUserDetailsService userDetailsService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             CustomUserDetailsService userDetailsService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Checks if the current user is an employee.
     *
     * @return true if logged‐in user has Employee role
     */
    private boolean isEmployee() {
        return userDetailsService.getLoggedInUser() instanceof Employee;
    }

    /** Supported product type identifiers for the UI. */
    private static final List<String> PRODUCT_TYPES = List.of(
            "FOOD", "DRINK", "DESSERT", "MILK_COCKTAIL"
    );

    /**
     * Shows the form to add a new product.
     * Only accessible by employees.
     *
     * @param model Spring MVC model
     * @return view name "product-add" or redirect to "/menu" if unauthorized
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        if (!isEmployee()) {
            return "redirect:/menu";
        }
        ProductEditDto dto = ProductEditDto.forCreate();
        model.addAttribute("productEditDto", dto);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("iceCreamTypes", IceCreamType.values());
        model.addAttribute("productTypes", PRODUCT_TYPES);
        model.addAttribute("mode", "add");
        model.addAttribute("formActionUrl", "/product/save");
        model.addAttribute("user", userDetailsService.getLoggedInUser());

        model.addAttribute("returnUrl", "/menu");

        return "product-add";
    }

    /**
     * Saves a newly created product.
     * Validates input and handles errors by redisplaying the form.
     *
     * @param dto   the DTO with product fields
     * @param model Spring MVC model
     * @return redirect to details page or redisplay form on validation error
     * @throws IOException if image upload fails
     */
    @PostMapping("/save")
    public String saveNewProduct(@ModelAttribute ProductEditDto dto,
                                 Model model) throws IOException {
        if (!isEmployee()) {
            return "redirect:/menu";
        }
        try {
            ProductDto saved = productService.addProduct(dto);
               return "redirect:/menu/details/" + saved.getId();
        } catch (ValidationException ex) {
            prepareForm(model, dto, "add", "/product/save", ex.getMessage());
            return "product-add";
        }
    }

    /**
     * Displays the details page for a product.
     *
     * @param id    product ID
     * @param model Spring MVC model
     * @return view name "product-details"
     */
    @GetMapping("/{id}/details")
    public String showDetails(@PathVariable Long id, Model model) {
        ProductDto dto = productService.getProductById(id);
        List<ImageDto> imgs = dto.getImages();

        String previewDataUri = "";
        if (!imgs.isEmpty()) {
            ImageDto chosen = imgs.stream()
                    .filter(ImageDto::isPreview)
                    .findFirst()
                    .orElse(imgs.get(0));
            if (chosen.getBase64Data() != null && !chosen.getBase64Data().isBlank()) {
                previewDataUri = "data:image/"
                        + chosen.getFormat().toLowerCase()
                        + ";base64,"
                        + chosen.getBase64Data();
            }
        }

        model.addAttribute("product", dto);
        model.addAttribute("previewImage", previewDataUri);
        model.addAttribute("productType", dto.getType());
        model.addAttribute("isEmployee", isEmployee());
        model.addAttribute("user", userDetailsService.getLoggedInUser());
        return "product-details";
    }


    /**
     * Shows the form to edit an existing product.
     * Only accessible by employees.
     *
     * @param id        product ID
     * @param returnUrl optional URL to return to after editing
     * @param model     Spring MVC model
     * @return view name "product-add" or redirect if unauthorized
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @RequestParam(required = false) String returnUrl,
                               Model model) {
        if (!isEmployee()) {
            return "redirect:/menu";
        }
        Product p = productService.getProductEntityById(id);
        ProductEditDto dto = ProductEditDto.fromEntity(p);
        model.addAttribute("productEditDto", dto);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("iceCreamTypes", IceCreamType.values());
        model.addAttribute("productTypes", PRODUCT_TYPES);
        model.addAttribute("mode", "edit");
        model.addAttribute("formActionUrl", "/product/edit/" + id);
        model.addAttribute("user", userDetailsService.getLoggedInUser());

        model.addAttribute("returnUrl", returnUrl != null ? returnUrl : "/menu/details/" + id);


        return "product-add";
    }

    /**
     * Saves edits to an existing product.
     * Validates input and handles errors by redisplaying the form.
     *
     * @param id    product ID
     * @param dto   the DTO with updated product fields
     * @param model Spring MVC model
     * @return redirect to details page or redisplay form on validation error
     * @throws IOException if image upload fails
     */
    @PostMapping("/edit/{id}")
    public String saveEditedProduct(@PathVariable Long id,
                                    @ModelAttribute ProductEditDto dto,
                                    Model model) throws IOException {
        if (!isEmployee()) {
            return "redirect:/menu";
        }
        try {
            productService.editProduct(id, dto);
               return "redirect:/menu/details/" + id;
        } catch (ValidationException ex) {
            prepareForm(model, dto, "edit", "/product/edit/" + id, ex.getMessage());
            return "product-add";
        }
    }


    /**
     * Deletes a product by ID.
     * Only accessible by employees.
     *
     * @param id product ID
     * @return redirect to "/menu"
     */
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        if (isEmployee()) {
            productService.removeProduct(id);
        }
        return "redirect:/menu";
    }

    /**
     * Populates the model with form attributes for add/edit operations.
     *
     * @param model     Spring MVC model
     * @param dto       the DTO bound to the form
     * @param mode      "add" or "edit"
     * @param actionUrl form submission URL
     * @param errorMsg  validation error message to display
     */
    private void prepareForm(Model model,
                             ProductEditDto dto,
                             String mode,
                             String actionUrl,
                             String errorMsg) {
        model.addAttribute("error", errorMsg);
        model.addAttribute("productEditDto", dto);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("iceCreamTypes", IceCreamType.values());
        model.addAttribute("productTypes", PRODUCT_TYPES);
        model.addAttribute("mode", mode);
        model.addAttribute("formActionUrl", actionUrl);
        model.addAttribute("user", userDetailsService.getLoggedInUser());


    }

    /**
     * Deletes a specific image from a product.
     *
     * @param prodId product ID
     * @param imgId  image ID
     * @return HTTP 204 No Content on successful deletion
     */
    @DeleteMapping("/{prodId}/images/{imgId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable("prodId") Long prodId,
            @PathVariable("imgId")  Long imgId) {

        // reuse your service layer
        productService.deleteImage(prodId, imgId);
        return ResponseEntity.noContent().build();
    }
}
