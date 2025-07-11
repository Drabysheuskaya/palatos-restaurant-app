package com.danven.web_library.service;

import com.danven.web_library.domain.user.User;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Unified user service that looks up a user by email from either customer or employee services.
 */
@Service
public class UserServiceImpl implements UserService {

    private final CustomerService customerService;
    private final EmployeeService employeeService;

    public UserServiceImpl(CustomerService customerService, EmployeeService employeeService) {
        this.customerService = customerService;
        this.employeeService = employeeService;
    }

    /**
     * Lookup a user by email address. First attempts to find a customer,
     * then an employee. Throws if no matching user is found.
     *
     * @param email the email address of the user to retrieve
     * @return the matching {@link User} (customer or employee)
     * @throws IllegalArgumentException if no user exists with the given email
     */
    @Override
    public User getUserByEmail(String email) {
        Optional<? extends User> customer = customerService.findOptionalByEmail(email);
        if (customer.isPresent()) {
            return customer.get();
        }

        Optional<? extends User> employee = employeeService.findOptionalByEmail(email);
        if (employee.isPresent()) {
            return employee.get();
        }

        throw new IllegalArgumentException("User not found with email: " + email);
    }
}
