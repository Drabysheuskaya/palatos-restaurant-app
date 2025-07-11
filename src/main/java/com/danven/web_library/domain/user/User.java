package com.danven.web_library.domain.user;

import com.danven.web_library.domain.config.custom_types.OptionalStringType;
import com.danven.web_library.domain.config.custom_validators.OptionalStringNotEmpty;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Abstract base class representing a user in the PalaTOS system.
 * Defines common persistent state and identity for all user roles.
 * Subclasses (e.g. {@code Customer}, {@code Employee}) inherit these fields
 * via JOINED inheritance strategy.
 */

@Entity
@Table(name = "PALATOS_USER")
@Inheritance(strategy = InheritanceType.JOINED)
@TypeDef(name = "optionalString", typeClass = OptionalStringType.class)
public abstract class User implements Serializable {

    /** Auto-generated unique identifier for the user. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    /** Mandatory first name of the user. */
    @NotBlank(message = "First name is required.")
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Optional surname of the user. If present, must not be empty.
     * Stored via a custom Hibernate type to support {@code Optional<String>}.
     */
    @OptionalStringNotEmpty(message = "Surname must not be blank if provided.")
    @Type(type = "optionalString")
    @Column(name = "surname")
    private Optional<String> surname = Optional.empty();

    /**
     * Timestamp when this user was registered.
     * Populated automatically on insert, never updated thereafter.
     */
    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registrationTime;

    /** Unique and valid email address used for login and communication. */
    @NotBlank(message = "Email is required.")
    @Email(message = "Email format is invalid.")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /**
     * Password for authentication.
     * Must be at least 8 characters long.
     */
    @Size(min = 8, message = "Password must be at least 8 characters.")
    @Column(name = "password", nullable = false)
    private String password;

    /** Indicates whether the user is active and can access the system. */
    @Column(name = "active", nullable = false)
    private boolean active;


    /**
     * Default constructor required by JPA.
     * Protected to prevent direct instantiation without setting required fields.
     */
    protected User() {}

    /**
     * Full constructor with optional surname.
     *
     * @param name     the user's first name
     * @param surname  the user's surname, optional
     * @param email    the user's email address
     * @param password the user's password
     * @param active   whether the user is active
     */
    protected User(String name, Optional<String> surname, String email, String password, boolean active) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.active = active;
    }
    /**
     * Alternative constructor for cases where surname is not provided.
     *
     * @param name     the user's first name
     * @param email    the user's email address
     * @param password the user's password
     * @param active   whether the user is active
     */

    protected User(String name, String email, String password, boolean active) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.active = active;
    }

    /**
     * @return the database identifier of this user, or {@code null} if not yet persisted
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the user's first name */
    public String getName() {
        return name;
    }

    /**
     * Change the user's first name.
     *
     * @param name new first name; must satisfy {@code @NotBlank}
     */
    public void setName(String name) {
        this.name = name;
    }

    /** @return optional surname */
    public Optional<String> getSurname() {
        return surname;
    }

    /**
     * Change the user's surname.
     * @param surname new surname wrapped in Optional.empty() or Optional.of(String)
     */
    public void setSurname(Optional<String> surname) {
        this.surname = surname;
    }

    /**
     * @return the timestamp when this user was created
     */
    public LocalDateTime getRegistrationTime() {
        return registrationTime;
    }

    /**
     * @return {@code true} if the user is currently active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Activate or deactivate this user.
     * @param active {@code true} to mark active; {@code false} to disable login
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the user's unique email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Change the user's email address.
     *
     * @param email new email, must be unique and valid
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return the user's password */
    public String getPassword() {
        return password;
    }

    /** @param password sets the user's password */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Two users are equal if they share the same ID (when persisted),
     * or otherwise the same unique email.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        if (id != null && other.id != null) {
            return id.equals(other.id);
        }
        return email.equals(other.email);
    }

    @Override
    public int hashCode() {
        return (id != null)
                ? id.hashCode()
                : email.hashCode();
    }

}


