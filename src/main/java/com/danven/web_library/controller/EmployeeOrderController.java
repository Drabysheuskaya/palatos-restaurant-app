// src/main/java/com/darina/PalaTOS/controller/EmployeeOrderController.java
package com.danven.web_library.controller;

import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.order.OrderStatus;
import com.danven.web_library.domain.order.PaymentStatus;
import com.danven.web_library.domain.product.ProductOrder;
import com.danven.web_library.dto.EmployeeProfileUpdateDto;
import com.danven.web_library.dto.ImageDto;
import com.danven.web_library.service.EmployeeService;
import com.danven.web_library.service.FeedbackService;
import com.danven.web_library.service.ImageService;
import com.danven.web_library.service.OrderPersistenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for employees to view and manage all customer orders.
 * Provides endpoints for profile updates, order listing, status updates,
 * cancellations, and feedback viewing.
 */
@Controller
@RequestMapping("/employee")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeOrderController {

    private final OrderPersistenceService orderService;
    private final EmployeeService         employeeService;
    private final ImageService            imageService;
    private final FeedbackService feedbackService;


    public EmployeeOrderController(OrderPersistenceService orderService,
                                   EmployeeService employeeService,
                                   ImageService imageService,
                                   FeedbackService feedbackService) {
        this.orderService    = orderService;
        this.employeeService = employeeService;
        this.imageService    = imageService;
        this.feedbackService = feedbackService;
    }

    /**
     * Updates the current employee's profile information.
     *
     * @param dto validated DTO containing profile fields to update
     * @return HTTP 200 OK if successful
     */

    @PutMapping("/me")
    public ResponseEntity<?> update(@Valid @RequestBody EmployeeProfileUpdateDto dto) {
        employeeService.updateProfile(dto);
        return ResponseEntity.ok().build();
    }
    /**
     * Displays the employee dashboard ("card") with all orders and related data.
     *
     * @param model     Spring MVC model for view attributes
     * @param principal security principal of the logged-in employee
     * @return the view name "employee/card"
     */
    @GetMapping("/card")
    public String viewEmployeeCard(Model model, Principal principal) {
        List<Order> allOrders = orderService.findAll();

        // Build a map: ProductOrder.id → List<ImageDto>
        Map<Long, List<ImageDto>> imageMap = allOrders.stream()
                .flatMap(o -> o.getProductOrders().stream())
                .collect(Collectors.toMap(
                        ProductOrder::getId,
                        po -> imageService.findAllByProduct(po.getProduct().getId())
                ));

        model.addAttribute("orders",   allOrders);
        model.addAttribute("imageMap", imageMap);
        model.addAttribute("user",     employeeService.getEmployeeByEmail(principal.getName()));
        model.addAttribute("activeTab","card");

        // CSRF token for  update/cancel forms
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            CsrfToken token =
                    (CsrfToken) attrs.getRequest().getAttribute("_csrf");
            model.addAttribute("_csrf", token);
        }

        // Map order ID to whether it has feedback
        Map<Long, Boolean> hasFeedbackMap = allOrders.stream()
                .collect(Collectors.toMap(
                        Order::getId,
                        o -> feedbackService.hasFeedbacks(o.getId())
                ));

        model.addAttribute("hasFeedbackMap", hasFeedbackMap);

        return "employee/card";
    }

    /**
     * Processes status and payment updates for a given order.
     * If both status=SERVED and paymentStatus=PAID, marks the order COMPLETED.
     *
     * @param orderId       ID of the order to update
     * @param status        new {@link OrderStatus} value
     * @param paymentStatus new {@link PaymentStatus} value
     * @return redirect to the employee card view
     */
    @PostMapping("/order/updateStatus")
    public String updateOrderStatus(@RequestParam Long orderId,
                                    @RequestParam String status,
                                    @RequestParam String paymentStatus) {

        Order o = orderService.findById(orderId);
        try {
            OrderStatus newStatus     = OrderStatus.valueOf(status);
            PaymentStatus newPayment  = PaymentStatus.valueOf(paymentStatus);

            if (o.getStatus() != OrderStatus.CANCELED
                    && o.getStatus() != OrderStatus.COMPLETED) {

                o.setStatus(newStatus);
                o.setPaymentStatus(newPayment);

                if (newStatus == OrderStatus.SERVED
                        && newPayment == PaymentStatus.PAID) {
                    o.setStatus(OrderStatus.COMPLETED);
                }

                orderService.save(o);
            }

        } catch (IllegalArgumentException ignored) {
            // invalid enum values – ignore
        }

        return "redirect:/employee/card";
    }

    /**
     * Cancels or deletes a NEW order based on the flag.
     * Canceled orders remain visible; deleted orders are removed entirely.
     *
     * @param orderId           ID of the order to cancel/delete
     * @param deleteCompletely  true to hard-delete, false to soft-cancel
     * @param request           HTTP servlet request for setting session flags
     * @return redirect to the employee card view
     */
    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam Long orderId,
                              @RequestParam boolean deleteCompletely,
                              HttpServletRequest request) {

        Order o = orderService.findById(orderId);
        if (o.getStatus() == OrderStatus.NEW) {
            if (deleteCompletely) {
                orderService.deleteById(orderId);
            } else {
                o.setStatus(OrderStatus.CANCELED);
                orderService.save(o);
            }
            // notify customer view
            request.getSession()
                    .setAttribute("employeeCanceledCustomerId",
                            o.getCustomer().getId());
            request.getSession().setAttribute("employeeCanceledOrder", true);
        }

        return "redirect:/employee/card";
    }

    /**
     * Shows all feedback entries for a specific order.
     *
     * @param id        the order ID for which to view feedback
     * @param model     Spring MVC model for view attributes
     * @param principal security principal of the logged-in employee
     * @return view name "employee/feedback-list"
     */
    @GetMapping("/order/{id}/feedbacks")
    public String viewFeedbacks(@PathVariable Long id,
                                Model model,
                                Principal principal) {
        Order order = orderService.findById(id);
        model.addAttribute("order",     order);
        model.addAttribute("feedbacks", feedbackService.getByOrder(id));

        model.addAttribute("user",      employeeService.getEmployeeByEmail(principal.getName()));
        model.addAttribute("activeTab","card");

        // CSRF
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            CsrfToken token = (CsrfToken) attrs.getRequest().getAttribute("_csrf");
            model.addAttribute("_csrf", token);
        }

        return "employee/feedback-list";
    }

}
