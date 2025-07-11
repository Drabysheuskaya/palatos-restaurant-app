// src/main/java/com/darina/PalaTOS/service/CustomerServiceImpl.java
package com.danven.web_library.service;

import com.danven.web_library.domain.user.Address;
import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.domain.user.User;
import com.danven.web_library.dto.CustomerProfileUpdateDto;
import com.danven.web_library.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of {@link CustomerService} that manages retrieval
 * and updating of {@link Customer} entities using the {@link UserRepository}.
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;

    public CustomerServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Retrieves the currently authenticated customer based on the
     * security context’s principal name (email).
     *
     * @return the authenticated {@link Customer}
     * @throws UsernameNotFoundException if no matching customer is found
     */
    @Override
    public Customer getAuthenticatedCustomer() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return getCustomerByEmail(email);
    }

    /**
     * Finds a {@link Customer} by their email address.
     *
     * @param email the email to look up
     * @return the {@link Customer} with the given email
     * @throws UsernameNotFoundException if the user is not found or is not a Customer
     */
    @Override
    public Customer getCustomerByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(u -> u instanceof Customer)
                .map(u -> (Customer) u)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Customer not found with email: " + email)
                );
    } /**
     * Finds a {@link Customer} by their email address.
     *
     * @param email the email to look up
     * @return the {@link Customer} with the given email
     * @throws UsernameNotFoundException if the user is not found or is not a Customer
     */
    @Override
    public Optional<? extends User> findOptionalByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(u -> u instanceof Customer);
    }

    /**
     * Converts a {@link Customer} entity into a {@link CustomerProfileUpdateDto}.
     * Used to prefill profile update forms.
     *
     * @param c the Customer to convert
     * @return a DTO containing the customer’s profile data
     */
    @Override
    public CustomerProfileUpdateDto toCustomerProfileDto(Customer c) {
        Address a = c.getAddress();
        return new CustomerProfileUpdateDto(
                c.getName(),
                c.getSurname().orElse(""),
                c.getEmail(),
                "",               // не предзаполняем пароль
                c.isActive(),
                c.getCustomerPhone(),
                c.getDateOfBirth(),
                a.getCountry(),
                a.getCity().orElse(""),
                a.getStreet().orElse(""),
                a.getHouseNumber().orElse(""),
                a.getPostalCode()
        );
    }

    /**
     * Applies updates from a {@link CustomerProfileUpdateDto} to the
     * authenticated Customer entity and persists the changes.
     * This operation is transactional.
     *
     * @param dto the profile update data
     * @return the updated and saved {@link Customer}
     */
    @Override
    @Transactional
    public Customer updateProfile(CustomerProfileUpdateDto dto) {
        Customer c = getAuthenticatedCustomer();
        c.setName(dto.getName());
        c.setSurname(Optional.ofNullable(dto.getSurname()));
        c.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            c.setPassword(dto.getPassword());
        }

        c.setActive(dto.isActive());
        c.setCustomerPhone(dto.getCustomerPhone());
        c.setDateOfBirth(dto.getDateOfBirth());

        Address updatedAddress = new Address(
                dto.getCountry(),
                Optional.ofNullable(dto.getCity()),
                Optional.ofNullable(dto.getStreet()),
                Optional.ofNullable(dto.getHouseNumber()),
                dto.getPostalCode()
        );
        c.setAddress(updatedAddress);

        return userRepository.save(c);
    }
}
