package com.danven.web_library.controller;

import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.domain.user.Employee;
import com.danven.web_library.dto.CustomerProfileUpdateDto;
import com.danven.web_library.dto.EmployeeProfileUpdateDto;
import com.danven.web_library.service.CustomerService;
import com.danven.web_library.service.EmployeeService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that routes users to their respective profile views
 * based on their role (Customer or Employee) and prepares data for display.
 */
@Controller
@RequestMapping("/profileTab")
public class ProfileTabController {

    private final CustomerService customerService;
    private final EmployeeService employeeService;

    public ProfileTabController(CustomerService customerService,
                                EmployeeService employeeService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

    /**
     * Redirects the authenticated user to either the customer or employee
     * profile view, depending on their granted authority.
     *
     * @param auth the current authentication context
     * @return redirect URL to the appropriate profile view
     */
    @GetMapping
    public String profileRedirect(Authentication auth) {
        boolean isCustomer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
        return isCustomer
                ? "redirect:/profileTab/customer/view"
                : "redirect:/profileTab/employee/view";
    }

    /**
     * Displays the profile page for the authenticated customer.
     * Loads the customer's data into a DTO for editing and view.
     *
     * @param model Spring MVC model for view attributes
     * @return view name "profile-customer"
     */
    @GetMapping("/customer/view")
    public String viewCustomer(Model model) {
        Customer c = customerService.getAuthenticatedCustomer();
        CustomerProfileUpdateDto dto = customerService.toCustomerProfileDto(c);
        model.addAttribute("profile", dto);
        model.addAttribute("user",    c);
        model.addAttribute("activeTab", "profileTab");
        return "profile-customer";
    }

    /**
     * Displays the profile page for the authenticated employee.
     * Loads the employee's data into a DTO for editing and view.
     *
     * @param model Spring MVC model for view attributes
     * @return view name "profile-employee"
     */
    @GetMapping("/employee/view")
    public String viewEmployee(Model model) {
        Employee e = employeeService.getAuthenticatedEmployee();
        EmployeeProfileUpdateDto dto = employeeService.toEmployeeProfileDto(e);
        model.addAttribute("profile", dto);
        model.addAttribute("user",    e);
        model.addAttribute("activeTab", "profileTab");
        return "profile-employee";
    }
}
