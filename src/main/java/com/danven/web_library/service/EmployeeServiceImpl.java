
package com.danven.web_library.service;

import com.danven.web_library.domain.user.Employee;
import com.danven.web_library.domain.user.User;
import com.danven.web_library.dto.EmployeeProfileUpdateDto;
import com.danven.web_library.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link EmployeeService} that manages retrieval
 * and updating of {@link Employee} entities using the {@link UserRepository}.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final UserRepository userRepository;

    public EmployeeServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Retrieves the currently authenticated employee based on the
     * security context’s principal name (email).
     *
     * @return the authenticated {@link Employee}
     * @throws UsernameNotFoundException if no matching employee is found
     */
    @Override
    public Employee getAuthenticatedEmployee() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return getEmployeeByEmail(email);
    }

    /**
     * Finds an {@link Employee} by their email address.
     *
     * @param email the email to look up
     * @return the {@link Employee} with the given email
     * @throws UsernameNotFoundException if the user is not found or is not an Employee
     */
    @Override
    public Employee getEmployeeByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(u -> u instanceof Employee)
                .map(u -> (Employee) u)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Employee not found with email: " + email)
                );
    }

    /**
     * Attempts to find an {@link Employee} by email, returning an Optional.
     *
     * @param email the email to search for
     * @return an Optional of the matching {@link Employee}, or empty if none found
     */
    @Override
    public Optional<? extends User> findOptionalByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(u -> u instanceof Employee);
    }

    /**
     * Converts an {@link Employee} entity into an {@link EmployeeProfileUpdateDto}.
     * Used to prefill profile update forms.
     *
     * @param e the Employee to convert
     * @return a DTO containing the employee’s profile data
     */
    @Override
    public EmployeeProfileUpdateDto toEmployeeProfileDto(Employee e) {
        return new EmployeeProfileUpdateDto(
                e.getName(),
                e.getSurname().orElse(""),
                e.getEmail(),
                "",             // не предзаполняем пароль
                e.isActive(),
                e.getEmployeeCardCode()
        );
    }

    /**
     * Applies updates from an {@link EmployeeProfileUpdateDto} to the
     * authenticated Employee entity and persists the changes.
     * This operation is transactional.
     *
     * @param dto the profile update data
     * @return the updated and saved {@link Employee}
     */
    @Override
    @Transactional
    public Employee updateProfile(EmployeeProfileUpdateDto dto) {
        Employee e = getAuthenticatedEmployee();
        e.setName(dto.getName());
        e.setSurname(Optional.ofNullable(dto.getSurname()));
        e.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            e.setPassword(dto.getPassword());
        }

        e.setActive(dto.isActive());

        // Validation is handled by @Valid in the controller

        return userRepository.save(e);
    }
}
