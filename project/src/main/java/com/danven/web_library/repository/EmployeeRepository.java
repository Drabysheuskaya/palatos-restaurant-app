package com.danven.web_library.repository;

import com.danven.web_library.domain.user.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository interface for accessing Employee entities from the database.
 * Includes queries for authentication, profile management, and identity lookup.
 */
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * Find employee by email address.
     * Used for login and profile update.
     *
     * @param email the email of the employee
     * @return an Optional containing the employee if found
     */
    Optional<Employee> findByEmail(String email);

    /**
     * Find employee by ID.
     * Useful for admin profile editing.
     *
     * @param id the ID of the employee
     * @return an Optional containing the employee if found
     */
    Optional<Employee> findById(Long id);

    /**
     * Find employee by unique employee card code.
     * Useful for identity verification or restricted actions.
     *
     * @param cardCode unique employee card code
     * @return an Optional containing the employee
     */
    @Query("SELECT e FROM Employee e WHERE e.employeeCardCode = :cardCode")
    Optional<Employee> findByEmployeeCardCode(@Param("cardCode") String cardCode);
}
