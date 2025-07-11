package com.danven.web_library.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO for updating customer profile information.
 * Carries only the fields that may be modified by the customer,
 * along with validation constraints.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CustomerProfileUpdateDto {

    /** Customer’s first name; must not be blank. */
    @NotBlank(message = "Name is required.")
    private String name;

    /** Customer’s surname; optional. */
    private String surname;

    /** Customer’s email address; must be valid and not blank. */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    private String email;

    /** New password; must be at least 8 characters if provided. */
    @Size(min = 8, message = "Password must be at least 8 characters.")
    private String password;

    /** Whether the customer’s account is active. */
    private boolean active;

    /** Contact phone number; must not be blank. */
    @NotBlank(message = "Phone number is required.")
    private String customerPhone;

    /** Customer’s date of birth; must not be null. */
    @NotNull(message = "Date of birth is required.")
    private LocalDate dateOfBirth;

    /** Customer’s country; must not be blank. */
    @NotBlank(message = "Country is required.")
    private String country;

    /** Customer’s city; optional. */
    private String city;

    /** Street name for the customer’s address; optional. */
    private String street;

    /** House number for the customer’s address; optional. */
    private String houseNumber;

    /** Postal code for the customer’s address; must not be blank. */
    @NotBlank(message = "Postal code is required.")
    private String postalCode;
}
