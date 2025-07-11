package com.danven.web_library.domain.user;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an employee with administrative privileges in the PalaTOS system.
 * Inherits all authentication and identity fields from {@link User} via
 * JOINED inheritance strategy. An {@code Employee} has a unique, immutable
 * card code and may perform order‐management operations.
 */
@Entity
@Table(name = "EMPLOYEE")
public class Employee extends User {

    /**
     * Unique, immutable employee card code assigned at creation.
     * Never modified after initialization.
     */
    @Column(name = "employee_card_code", nullable = false, unique = true, updatable = false)
    @NotBlank(message = "Employee card code is required.")
    private String employeeCardCode;

    /**
     * Protected default constructor required by JPA.
     * Use {@link #Employee(String, Optional, String, String, boolean, String)}
     * or {@link #Employee(String, String, String, boolean, String)} to create instances.
     */
    protected Employee() {
        super();
    }

    /**
     * Constructs a new {@code Employee} with an optional surname.
     *
     * @param name              the employee's first name; must be non-blank
     * @param surname           the employee's surname wrapped in {@link Optional}, if present must be non-empty
     * @param email             the employee's unique email; must be valid format
     * @param password          the employee's password; must be at least 8 characters
     * @param active            whether the employee’s account is active
     * @param employeeCardCode  unique, immutable card code for the employee
     */
    public Employee(String name, Optional<String> surname, String email, String password, boolean active, String employeeCardCode) {
        super(name, surname, email, password, active);
        this.employeeCardCode = employeeCardCode;
    }

    /**
     * Constructs a new {@code Employee} without a surname.
     *
     * @param name              the employee's first name; must be non-blank
     * @param email             the employee's unique email; must be valid format
     * @param password          the employee's password; must be at least 8 characters
     * @param active            whether the employee’s account is active
     * @param employeeCardCode  unique, immutable card code for the employee
     */
    public Employee(String name, String email, String password, boolean active, String employeeCardCode) {
        super(name, email, password, active);
        this.employeeCardCode = employeeCardCode;
    }

    /**
     * Returns the immutable employee card code.
     *
     * @return the card code assigned to this employee
     */
    public String getEmployeeCardCode() {
        return this.employeeCardCode;
    }


    /**
     * Two {@code Employee} instances are equal if their parent {@linkplain User#equals(Object) identity}
     * matches and they share the same {@linkplain #employeeCardCode card code}.
     * @param o the object to compare with
     * @return {@code true} if equal by both identity and card code, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee employee)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(employeeCardCode, employee.employeeCardCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), employeeCardCode);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "employeeCardCode='" + employeeCardCode + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", active=" + isActive() +
                '}';
    }
}
