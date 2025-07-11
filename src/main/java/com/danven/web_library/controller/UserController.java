// src/main/java/com/darina/PalaTOS/controller/UserController.java
package com.danven.web_library.controller;

import com.danven.web_library.config.CustomUserDetailsService;
import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.domain.user.Employee;
import com.danven.web_library.dto.CategoryDto;
import com.danven.web_library.dto.ProductDto;
import com.danven.web_library.dto.ProductEditDto;
import com.danven.web_library.service.CategoryService;
import com.danven.web_library.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;


/**
 * Handles user‐facing pages: login, profile redirect, and offers listing/detail/update.
 * Differentiates behavior based on whether the user is a Customer or Employee.
 */
@Controller
public class UserController {

    private final CategoryService            categoryService;
    private final ProductService             productService;
    private final CustomUserDetailsService   customUserDetailsService;

    public UserController(CategoryService categoryService,
                          ProductService productService,
                          CustomUserDetailsService customUserDetailsService) {
        this.categoryService           = categoryService;
        this.productService            = productService;
        this.customUserDetailsService  = customUserDetailsService;
    }

    /**
     * Displays the login page, optionally showing a message or error.
     *
     * @param model Spring MVC model
     * @param msg   optional informational message
     * @param error optional error message
     * @return view name "login"
     */
    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(name = "msg", required = false) String msg,
                            @RequestParam(name = "error", required = false) String error) {
        model.addAttribute("msg",   msg);
        model.addAttribute("error", error);
        model.addAttribute("activeTab", "login");
        model.addAttribute("user", customUserDetailsService.getLoggedInUserOrNull());
        return "login";
    }

    /**
     * Redirects authenticated users to the appropriate profile view
     * or to the login page if not authenticated.
     *
     * @return redirect URL to profile view or login
     */
    @GetMapping("/profile")
    public String profileRedirect() {
        var u = customUserDetailsService.getLoggedInUserOrNull();
        if      (u instanceof Customer) return "redirect:/profile/customer/view";
        else if (u instanceof Employee)  return "redirect:/profile/employee/view";
        else    return "redirect:/login";
    }

    /**
     * Shows the offers page with all products and categories.
     * Caches the product list in the session.
     *
     * @param model   Spring MVC model
     * @param session HTTP session for caching the offers list
     * @return view name "profile_offers"
     */
    @GetMapping("/profile/offers")
    public String offerPage(Model model, HttpSession session) {
        // load DTOs, each ProductDto.images[].base64Data already contains the "data:image/…;base64,…" string
        List<ProductDto> products = productService.getAllProducts();

        model.addAttribute("products", products);
        model.addAttribute("categories",
                categoryService.getAllCategories()
                        .stream()
                        .map(c -> new CategoryDto(c.getId(), c.getName()))
                        .toList()
        );
        model.addAttribute("user", customUserDetailsService.getLoggedInUserOrNull());
        model.addAttribute("activeTab", "offers");

        session.setAttribute("offerList", products);

        return "profile_offers";
    }

    /**
     * Displays detailed view of a single product, retrieving it from session cache
     * or falling back to service lookup.
     *
     * @param productId the ID of the product to display
     * @param model     Spring MVC model
     * @param session   HTTP session for retrieving the cached offers list
     * @return view name "product_details"
     */
    @GetMapping("/profile/offers/details")
    public String getProductDetails(@RequestParam("productId") Long productId,
                                    Model model,
                                    HttpSession session) {

        @SuppressWarnings("unchecked")
        var products = (List<ProductDto>) session.getAttribute("offerList");

        ProductDto productDto = null;
        if (products != null) {
            productDto = products.stream()
                    .filter(p -> p.getId().equals(productId))
                    .findFirst()
                    .orElse(null);
        }
        // fallback
        if (productDto == null) {
            productDto = productService.getProductById(productId);
        }

        model.addAttribute("product",    productDto);
        model.addAttribute("categories", categoryService.getAllCategories()
                .stream()
                .map(c -> new CategoryDto(c.getId(), c.getName()))
                .toList());
        model.addAttribute("user",       customUserDetailsService.getLoggedInUserOrNull());
        model.addAttribute("activeTab",  "offers");

        return "product_details";
    }

    /**
     * Processes updates submitted from the product details page.
     * Only employees may update; errors are flashed back to the form.
     *
     * @param productId         the ID of the product to update
     * @param productEditDTO    bound DTO containing updated fields
     * @param redirectAttributes attributes for passing flash messages
     * @return redirect to offers list or back to the details page on error
     */
    @PostMapping("/profile/offers/details/update")
    public String updateProduct(@RequestParam("productId") Long productId,
                                @ModelAttribute ProductEditDto productEditDTO,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.editProduct(productId, productEditDTO);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile/offers/details?productId=" + productId;
        }
        return "redirect:/profile/offers";
    }
}
