package com.danven.web_library.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Data Transfer Object for updating employee profile information.
 * Contains only the fields that an employee is allowed to modify,
 * along with validation constraints to ensure data integrity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeProfileUpdateDto {

    /** Employee’s first name; must not be blank. */
    @NotBlank(message = "Name is required.")
    private String name;

    /** Employee’s surname; optional. */
    private String surname;

    /** Employee’s email address; must be valid and not blank. */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    private String email;

    /** New password; must be at least 8 characters if provided. */
    @Size(min = 8, message = "Password must be at least 8 characters.")
    private String password;

    /** Whether the employee’s account is active. */
    private boolean active;

    /** Unique card code assigned to the employee; optional. */
    private String employeeCardCode;
}
