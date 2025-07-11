package com.danven.web_library.domain.user;

import com.danven.web_library.domain.config.custom_types.OptionalStringType;
import com.danven.web_library.domain.config.custom_validators.OptionalStringNotEmpty;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a customer's address.
 * Used as an embeddable complex attribute inside the Customer entity.
 */
@Embeddable
@TypeDefs({
        @TypeDef(name = "optionalString", typeClass = OptionalStringType.class)
})
public class Address implements Serializable {

    /** Country must be provided and cannot be empty. */
    @NotEmpty(message = "Country is required.")
    @Column(name = "country", nullable = false)
    private String country;

    /** City is optional but cannot be blank if present. */
    @OptionalStringNotEmpty(message = "City must not be blank if provided.")
    @Type(type = "optionalString")
    @Column(name = "city")
    private Optional<String> city = Optional.empty();

    /** Street is optional but cannot be blank if present. */
    @OptionalStringNotEmpty(message = "Street must not be blank if provided.")
    @Type(type = "optionalString")
    @Column(name = "street")
    private Optional<String> street = Optional.empty();

    /** House number is optional but must not be blank if present. */
    @OptionalStringNotEmpty(message = "House number must not be blank if provided.")
    @Type(type = "optionalString")
    @Column(name = "number_of_house")
    private Optional<String> houseNumber = Optional.empty();

    /** Postal code must be provided and cannot be empty. */
    @NotEmpty(message = "Postal code is required.")
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    /**
     * Default no-args constructor required by JPA.
     */
    protected Address() {
    }

    /**
     * Full constructor for all fields.
     *
     * @param country     the country name (required)
     * @param city        optional city
     * @param street      optional street
     * @param houseNumber optional house number
     * @param postalCode  postal code (required)
     */
    public Address(String country,
                   Optional<String> city,
                   Optional<String> street,
                   Optional<String> houseNumber,
                   String postalCode) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
    }

    /** @return country part of the address */
    public String getCountry() {
        return country;
    }

    /** @param country sets the country */
    public void setCountry(String country) {
        this.country = country;
    }

    /** @return optional city part */
    public Optional<String> getCity() {
        return city;
    }

    /** @param city sets the optional city */
    public void setCity(Optional<String> city) {
        this.city = city;
    }

    /** @return optional street part */
    public Optional<String> getStreet() {
        return street;
    }

    /** @param street sets the optional street */
    public void setStreet(Optional<String> street) {
        this.street = street;
    }

    /** @return optional house number */
    public Optional<String> getHouseNumber() {
        return houseNumber;
    }

    /** @param houseNumber sets the optional house number */
    public void setHouseNumber(Optional<String> houseNumber) {
        this.houseNumber = houseNumber;
    }

    /** @return postal code of the address */
    public String getPostalCode() {
        return postalCode;
    }

    /** @param postalCode sets the postal code */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(country, address.country)
                && Objects.equals(city, address.city)
                && Objects.equals(street, address.street)
                && Objects.equals(houseNumber, address.houseNumber)
                && Objects.equals(postalCode, address.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, city, street, houseNumber, postalCode);
    }

    @Override
    public String toString() {
        return "Address{" +
                "country='" + country + '\'' +
                ", city=" + city +
                ", street=" + street +
                ", houseNumber=" + houseNumber +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
}
