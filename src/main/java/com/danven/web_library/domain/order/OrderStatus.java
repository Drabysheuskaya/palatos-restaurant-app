package com.danven.web_library.domain.order;

/**
 * Represents the types of offers available in the library system.
 */
public enum OrderStatus {
    NEW, IN_PROGRESS, SERVED, CANCELED, COMPLETED;

    public boolean isServed() {
        return this == SERVED || this == COMPLETED;
    }
}
