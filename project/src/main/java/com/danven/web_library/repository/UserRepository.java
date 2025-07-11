package com.danven.web_library.repository;

import com.danven.web_library.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing User entities in the database.
 * Extends JpaRepository to inherit basic CRUD operations.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves an Optional<User> from the database by its email address.
     *
     * @param email The email address of the user to retrieve.
     * @return Optional<User> containing the user with the specified email, if found.
     *         Empty Optional if no user exists with the given email.
     */
    Optional<User> findByEmail(String email);
}
