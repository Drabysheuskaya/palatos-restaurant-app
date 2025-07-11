package com.danven.web_library.service;


import com.danven.web_library.domain.user.Employee;
import com.danven.web_library.domain.user.User;
import com.danven.web_library.dto.EmployeeProfileUpdateDto;

import java.util.Optional;

/**
 * Service interface for handling Employee-related operations.
 */
public interface EmployeeService {

    /**
     * Get the currently logged-in employee.
     */
    Employee getAuthenticatedEmployee();

    /**
     * Fetch an employee by email.
     */
    Employee getEmployeeByEmail(String email);

    /**
     * Optional-based lookup (general use).
     */
    Optional<? extends User> findOptionalByEmail(String email);

    /**
     * Convert Employee → DTO for pre-filling the edit form.
     */
    EmployeeProfileUpdateDto toEmployeeProfileDto(Employee employee);

    /**
     * Update employee's profile from the DTO.
     */
    Employee updateProfile(EmployeeProfileUpdateDto dto);

}
