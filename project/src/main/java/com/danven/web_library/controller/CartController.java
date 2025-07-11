package com.danven.web_library.controller;

import com.danven.web_library.config.CustomUserDetailsService;
import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.order.OrderService;
import com.danven.web_library.domain.order.OrderStatus;
import com.danven.web_library.domain.order.PaymentStatus;
import com.danven.web_library.domain.product.ProductOrder;
import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.domain.user.User;
import com.danven.web_library.dto.ImageDto;
import com.danven.web_library.repository.OrderServiceRepository;
import com.danven.web_library.service.CustomerService;
import com.danven.web_library.service.ImageService;
import com.danven.web_library.service.OrderPersistenceService;
import com.danven.web_library.service.ProductService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for managing the shopping cart (in‐session order) operations:
 * viewing, adding, updating quantities, clearing, and submitting orders.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final ProductService           productService;
    private final ImageService             imageService;
    private final CustomUserDetailsService userDetailsService;
    private final OrderPersistenceService  orderPersister;
    private final OrderServiceRepository   orderServiceRepo;
    private final CustomerService          customerService;

    public CartController(ProductService productService,
                          ImageService imageService,
                          CustomUserDetailsService userDetailsService,
                          OrderPersistenceService orderPersister,
                          OrderServiceRepository orderServiceRepo,
                          CustomerService customerService) {
        this.productService     = productService;
        this.imageService       = imageService;
        this.userDetailsService = userDetailsService;
        this.orderPersister     = orderPersister;
        this.orderServiceRepo   = orderServiceRepo;
        this.customerService    = customerService;
    }

    /**
     * Initializes a new Order for the logged-in Customer.
     *
     * @return a new {@link Order} in NEW/UNPAID state, or null if current user is not a Customer
     */
    private Order initOrder() {
        User u = userDetailsService.getLoggedInUser();
        if (!(u instanceof Customer)) {
            return null;
        }
        Order o = new Order();
        o.setCustomer((Customer) u);
        o.setStatus(OrderStatus.NEW);
        o.setPaymentStatus(PaymentStatus.UNPAID);
        o.setOrderTime(LocalDateTime.now());
        OrderService svc = orderServiceRepo.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Missing ORDER_SERVICE id=1"));
        o.setOrderService(svc);
        return o;
    }

    /**
     * Displays the current cart contents or redirects to the orders page if empty.
     *
     * @param session the HTTP session in which the cart is stored
     * @param model   the Spring MVC model for view rendering
     * @return the "cart" view name or a redirect to "/orders" or "/menu"
     */
    @GetMapping
    public String viewCartOrOrders(HttpSession session, Model model) {
        Order cart = (Order) session.getAttribute("order");
        if (cart == null) {
            cart = initOrder();
            if (cart == null) {
                return "redirect:/menu";
            }
            session.setAttribute("order", cart);
        }

        // If the cart is empty, redirect into your normal /orders flow
        if (cart.getProductOrders().isEmpty()) {
            return "redirect:/orders";
        }

        // Otherwise, render the live cart exactly as before
        List<ProductOrder> items = new ArrayList<>(cart.getProductOrders());
        model.addAttribute("items",         items);
        model.addAttribute("productsTotal", BigDecimal.valueOf(cart.calculateTotalAmount()));
        model.addAttribute("serviceFee",    BigDecimal.valueOf(cart.calculateServiceFee()));
        model.addAttribute("orderTotal",    BigDecimal.valueOf(cart.calculateFinalPrice()));
        model.addAttribute("dateTime",
                cart.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("user",          cart.getCustomer());
        model.addAttribute("activeTab",     "cart");

        // Build previewMap: productId → single base64 preview image
        Map<Long,String> previewMap = items.stream()
                .map(po -> po.getProduct().getId())
                .distinct()
                .collect(Collectors.toMap(
                        pid -> pid,
                        pid -> imageService.findAllByProduct(pid).stream()
                                .filter(ImageDto::isPreview)
                                .map(ImageDto::getBase64Data)
                                .findFirst()
                                .orElse(""),    // empty = no preview
                        (a,b)->a             // in case of duplicates
                ));
        model.addAttribute("previewMap", previewMap);

        // CSRF token (for inline clear/update)
        CsrfToken csrf = (CsrfToken)((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes())
                .getRequest().getAttribute("_csrf");
        model.addAttribute("_csrf", csrf);

        return "cart";
    }

    /**
     * Adds one unit of the specified product to the cart,
     * creating a new ProductOrder if needed.
     *
     * @param id      the product ID to add
     * @param session the HTTP session containing the cart
     * @return redirect back to "/cart"
     */
    @GetMapping("/add/{id}")
    public String addToCart(@PathVariable Long id,
                            HttpSession session) {
        Order cart = (Order) session.getAttribute("order");
        if (cart == null) {
            cart = initOrder();
            session.setAttribute("order", cart);
        }
        Order finalCart = cart;
        cart.getProductOrders().stream()
                .filter(po -> po.getProduct().getId().equals(id))
                .findFirst()
                .ifPresentOrElse(
                        po -> po.setAmount(po.getAmount() + 1),
                        () -> finalCart.addProductOrder(new ProductOrder(
                                productService.getProductEntityById(id),
                                finalCart, 1,
                                productService.getProductById(id).getPrice()
                        ))
                );
        return "redirect:/cart";
    }

    /**
     * Updates the quantity of a product in the cart.
     *
     * @param productId the ID of the product to update
     * @param qty       the new quantity
     * @param session   the HTTP session containing the cart
     * @return redirect back to "/cart"
     */
    @PostMapping("/update")
    public String updateQty(@RequestParam Long productId,
                            @RequestParam int qty,
                            HttpSession session) {
        Order cart = (Order) session.getAttribute("order");
        if (cart != null) {
            cart.getProductOrders().stream()
                    .filter(po -> po.getProduct().getId().equals(productId))
                    .findFirst()
                    .ifPresent(po -> po.setAmount(qty));
        }
        return "redirect:/cart";
    }

    /**
     * Clears the current cart by removing it from the session.
     *
     * @param session the HTTP session to clear
     * @return redirect to "/menu"
     */
    @PostMapping("/clear")
    public String clearCart(HttpSession session) {
        session.removeAttribute("order");
        return "redirect:/menu";
    }

    /**
     * Submits the current cart as a finalized order:
     * synchronizes quantities, sets metadata, persists, and clears session.
     *
     * @param tableNumber the table number for the order
     * @param notes       any customer notes
     * @param productId   list of product IDs from the form
     * @param qty         corresponding list of quantities
     * @param session     the HTTP session containing the cart
     * @return redirect to "/orders" after submission
     */
    @PostMapping("/submit")
    public String submitOrder(
            @RequestParam int tableNumber,
            @RequestParam String notes,
            @RequestParam List<Long> productId,
            @RequestParam List<Integer> qty,
            HttpSession session) {

        Order cart = (Order) session.getAttribute("order");
        if (cart == null) {
            return "redirect:/menu";
        }

        // 1) Sync the quantities from the form into the in-memory cart
        Map<Long,Integer> qtyMap = new HashMap<>();
        for (int i = 0; i < productId.size(); i++) {
            qtyMap.put(productId.get(i), qty.get(i));
        }
        cart.getProductOrders().forEach(po -> {
            Integer newQty = qtyMap.get(po.getProduct().getId());
            if (newQty != null) {
                po.setAmount(newQty);
            }
        });

        // 2) Set the rest of your order metadata
        cart.setTableNumber(tableNumber);
        cart.setNotes(notes);
        cart.setOrderTime(LocalDateTime.now());

        // 3) Persist and clear the session
        orderPersister.save(cart);
        session.removeAttribute("order");

        return "redirect:/orders";
    }

}
