package com.danven.web_library.controller;


import com.danven.web_library.config.CustomUserDetailsService;
import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.order.OrderStatus;
import com.danven.web_library.domain.order.PaymentStatus;
import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.domain.user.User;
import com.danven.web_library.dto.FeedbackDto;
import com.danven.web_library.dto.ImageDto;
import com.danven.web_library.service.FeedbackService;
import com.danven.web_library.service.ImageService;
import com.danven.web_library.service.OrderPersistenceService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller handling customer order operations:
 * listing, viewing details, canceling, reactivating, paying,
 * and submitting feedback.
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderPersistenceService orderService;
    private final ImageService imageService;
    private final CustomUserDetailsService userDetails;
    private final FeedbackService feedbackService;

    public OrderController(OrderPersistenceService orderService,
                           ImageService imageService,
                           CustomUserDetailsService userDetails,
                           FeedbackService feedbackService) {
        this.orderService = orderService;
        this.imageService = imageService;
        this.userDetails  = userDetails;
        this.feedbackService = feedbackService;
    }

    /**
     * Injects enum constants into the model for use in views.
     *
     * @param model the Spring MVC model
     */
    private void injectEnums(Model model) {
        model.addAttribute("NEW_STATUS",        OrderStatus.NEW);
        model.addAttribute("IN_PROGRESS_STATUS",OrderStatus.IN_PROGRESS);
        model.addAttribute("SERVED_STATUS",     OrderStatus.SERVED);
        model.addAttribute("COMPLETED_STATUS",  OrderStatus.COMPLETED);
        model.addAttribute("CANCELED_STATUS",   OrderStatus.CANCELED);
        model.addAttribute("UNPAID_STATUS",     PaymentStatus.UNPAID);
        model.addAttribute("PAID_STATUS",       PaymentStatus.PAID);
    }

    /**
     * Lists the current customer's orders, excluding canceled unless just canceled.
     *
     * @param model   Spring MVC model
     * @param request HTTP request for session flags
     * @return view name "order/list" or redirect to "/menu" if not a customer
     */
    @GetMapping
    public String listOrders(Model model, HttpServletRequest request) {
        User u = userDetails.getLoggedInUser();
        if (!(u instanceof Customer)) {
            return "redirect:/menu";
        }

        // Check session flags for recently canceled orders
        Boolean justCanceled = Boolean.TRUE.equals(
                request.getSession().getAttribute("customerCanceledOrder")
        );
        Long    canceledId  = (Long) request.getSession().getAttribute("customerCanceledOrderId");

        if (justCanceled && canceledId != null) {
            // show banner for this order
            request.getSession().removeAttribute("customerCanceledOrder");
            request.getSession().removeAttribute("customerCanceledOrderId");
        }

        Boolean empCanceled = Boolean.TRUE.equals(
                request.getSession().getAttribute("employeeCanceledOrder")
        );
        if (empCanceled) {
            model.addAttribute("orderCanceledByEmployee", true);
            request.getSession().removeAttribute("employeeCanceledOrder");
        }

        // Filter orders: exclude canceled except recently canceled by this customer
        List<Order> orders = orderService.findByCustomer((Customer)u).stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELED
                        || (justCanceled && o.getId().equals(canceledId)))
                .collect(Collectors.toList());

        // Build preview image map productId → base64
        Map<Long,String> previewMap = orders.stream()
                .flatMap(o -> o.getProductOrders().stream())
                .collect(Collectors.toMap(
                        po -> po.getProduct().getId(),
                        po -> imageService.findAllByProduct(po.getProduct().getId()).stream()
                                .filter(ImageDto::isPreview)
                                .map(ImageDto::getBase64Data)
                                .findFirst().orElse(""),
                        (a, b) -> a
                ));

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        CsrfToken csrf = null;
        if (attrs != null) {
            csrf = (CsrfToken) attrs.getRequest().getAttribute("_csrf");
        }


        model.addAttribute("orders",    orders);
        model.addAttribute("previewMap", previewMap);
        model.addAttribute("_csrf",      csrf);
        model.addAttribute("user",       u);
        model.addAttribute("activeTab",  "orders");
        injectEnums(model);
        return "order/list";
    }


    /**
     * Displays the details of a specific order if owned by the customer.
     *
     * @param id      order ID
     * @param model   Spring MVC model
     * @param request HTTP request for session flags
     * @return view name "order/detail" or redirect if access denied or canceled
     */
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id,
                            Model model,
                            HttpServletRequest request) {
        User u = userDetails.getLoggedInUser();
        Order o = orderService.findById(id);

        // Prevent viewing an order canceled by an employee
        if (o.getStatus() == OrderStatus.CANCELED) {
            request.getSession().setAttribute("employeeCanceledOrder", true);
            return "redirect:/orders";
        }

        // Ensure only the owning customer can view
        if (!(u instanceof Customer) || !o.getCustomer().equals(u)) {
            return "redirect:/orders";
        }

        // Build preview map as above
        Map<Long, String> previewMap = o.getProductOrders().stream()
                .map(po -> po.getProduct().getId())
                .distinct()
                .collect(Collectors.toMap(
                        pid -> pid,
                        pid -> imageService.findAllByProduct(pid).stream()
                                .filter(ImageDto::isPreview)
                                .map(ImageDto::getBase64Data)
                                .findFirst().orElse(""),
                        (a, b) -> a
                ));

        CsrfToken csrf = (CsrfToken)((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes())
                .getRequest().getAttribute("_csrf");

        model.addAttribute("order",      o);
        model.addAttribute("previewMap", previewMap);
        model.addAttribute("user",       u);
        model.addAttribute("_csrf",      csrf);
        model.addAttribute("activeTab",  "orders");
        injectEnums(model);
        return "order/detail";
    }

    /**
     * Cancels the given order (sets status to CANCELED) and marks session flag.
     *
     * @param id      order ID to cancel
     * @param request HTTP request to set cancellation flag
     * @return redirect to "/orders"
     */
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         HttpServletRequest request) {
        orderService.cancel(id);
        request.getSession().setAttribute("customerCanceledOrder", true);
        request.getSession().setAttribute("customerCanceledOrderId", id);

        return "redirect:/orders";
    }


    /**
     * Reactivates a previously canceled order.
     *
     * @param id order ID to reactivate
     * @return redirect to "/orders"
     */
    @PostMapping("/{id}/reactivate")
    public String reactivate(@PathVariable Long id) {
        orderService.reactivate(id);
        return "redirect:/orders";
    }

    /**
     * Deletes an order permanently.
     *
     * @param id order ID to delete
     * @return redirect to "/orders"
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        orderService.deleteById(id);
        return "redirect:/orders";
    }


    /**
     * Marks the order as paid via card.
     *
     * @param id order ID to pay
     * @return redirect to "/orders"
     */
    @PostMapping("/{id}/pay/card")
    public String payCard(@PathVariable Long id) {
        orderService.pay(id, PaymentStatus.PAID);
        return "redirect:/orders";
    }

    /**
     * Marks the order as paid via cash.
     *
     * @param id order ID to pay
     * @return redirect to "/orders"
     */
    @PostMapping("/{id}/pay/cash")
    public String payCash(@PathVariable Long id) {
        orderService.pay(id, PaymentStatus.PAID);
        return "redirect:/orders";
    }

    /**
     * Shows the feedback form for a completed order.
     *
     * @param id      order ID
     * @param model   Spring MVC model
     * @param request HTTP request for CSRF token
     * @return view name "feedbackForm" or redirect if order not completed
     */
    @GetMapping("/{id}/feedback")
    public String feedbackForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        Order order = orderService.findById(id);
        if (order.getStatus() != OrderStatus.COMPLETED) {
            return "redirect:/orders";
        }

        // 1)DTO and Order
        FeedbackDto dto = new FeedbackDto();
        dto.setSubmittedAt(LocalDateTime.now());   // ← сюда
        model.addAttribute("feedbackDto", dto);
        model.addAttribute("order", order);

        // CSRF
        CsrfToken csrf = (CsrfToken)((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes())
                .getRequest().getAttribute("_csrf");
        model.addAttribute("_csrf", csrf);

        model.addAttribute("user", userDetails.getLoggedInUser());

        model.addAttribute("activeTab", "orders");
        injectEnums(model);
        return "feedbackForm";
    }

    /**
     * Processes submitted feedback for an order.
     *
     * @param id   order ID
     * @param dto  validated feedback data
     * @param br   binding result for validation errors
     * @param model Spring MVC model for redisplay on error
     * @return redirect to "/orders" or back to form if errors exist
     */
    @PostMapping("/{id}/feedback")
    public String leaveFeedback(@PathVariable Long id,
                                @ModelAttribute @Valid FeedbackDto dto,
                                BindingResult br,
                                Model model) {
        if (br.hasErrors()) {
            model.addAttribute("order", orderService.findById(id));
            return "feedbackForm";
        }
        dto.setSubmittedAt(LocalDateTime.now());
        feedbackService.leaveFeedback(id, dto.getDescription());
        return "redirect:/orders";
    }

}
