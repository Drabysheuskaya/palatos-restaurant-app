package com.danven.web_library.repository;

import com.danven.web_library.domain.user.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository interface for accessing {@link Customer} entities.
 * Extends {@link JpaRepository} to provide standard CRUD methods.
 * Includes methods to load a Customer alone or together with their
 * feedbacks and orders via JPQL fetch joins, without using EntityGraph.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Retrieves a customer by their email address.
     * Loads only the Customer entity itself.
     *
     * @param email the email address to search for
     * @return an Optional containing the matching customer, or empty if not found
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Retrieves a customer by ID, fetching their feedbacks and orders eagerly.
     *
     * @param id the ID of the customer
     * @return an Optional containing the customer with feedbacks and orders loaded
     */
    @Query("""
        SELECT DISTINCT c
        FROM Customer c
        LEFT JOIN FETCH c.feedbacks
        LEFT JOIN FETCH c.orders
        WHERE c.id = :id
    """)
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);

    /**
     * Retrieves a customer by email, fetching their feedbacks and orders eagerly.
     *
     * @param email the email of the customer
     * @return an Optional containing the customer with feedbacks and orders loaded
     */
    @Query("""
        SELECT DISTINCT c
        FROM Customer c
        LEFT JOIN FETCH c.feedbacks
        LEFT JOIN FETCH c.orders
        WHERE c.email = :email
    """)
    Optional<Customer> findByEmailWithDetails(@Param("email") String email);
}
