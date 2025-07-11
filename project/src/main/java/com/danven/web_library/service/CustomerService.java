package com.danven.web_library.service;

import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.domain.user.User;
import com.danven.web_library.dto.CustomerProfileUpdateDto;

import java.util.Optional;

/**
 * Service interface for handling Customer-related operations.
 */
public interface CustomerService {

    /**
     * Returns the currently authenticated customer.
     */
    Customer getAuthenticatedCustomer();

    /**
     * Finds a customer by email (if exists).
     */
    Customer getCustomerByEmail(String email);

    /**
     * Optional-based lookup (used in general user flows).
     */
    Optional<? extends User> findOptionalByEmail(String email);

    /**
     * Converts a Customer entity into a DTO for editing profile.
     */
    CustomerProfileUpdateDto toCustomerProfileDto(Customer customer);

    /**
     * Updates customer entity with submitted DTO.
     */
    Customer updateProfile(CustomerProfileUpdateDto dto);



}
