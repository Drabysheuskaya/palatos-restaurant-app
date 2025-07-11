package com.danven.web_library.domain.order;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.ValidationException;
import java.time.LocalDateTime;

/**
 * Applies a holiday‐specific 10% discount to all orders placed within the defined holiday period.
 * Extends {@link OrderService} with additional fields for holiday name and the applicable time window.
 */
@Entity
@Table(name = "HOLIDAY_ORDER_SERVICE")
public class HolidayOrderService extends OrderService {

    /**
     * Name of the holiday (e.g., "Christmas", "New Year").
     */
    @Column(name = "holiday_name", nullable = false)
    private String holidayName;

    /**
     * Inclusive start of the holiday period.
     */
    @Column(name = "holiday_start_time", nullable = false)
    private LocalDateTime holidayStartTime;

    /**
     * Inclusive end of the holiday period.
     */
    @Column(name = "holiday_end_time", nullable = false)
    private LocalDateTime holidayEndTime;

    /**
     * Default constructor for JPA.
     */
    protected HolidayOrderService() {
        super();
    }

    /**
     * Constructs a HolidayOrderService with a fixed 10% discount rate,
     * active during the specified holiday window.
     *
     * @param serviceName       display name of this service
     * @param holidayName       non‐null, non‐blank name of the holiday
     * @param holidayStartTime  start of the holiday period (inclusive)
     * @param holidayEndTime    end of the holiday period (inclusive); must not be before {@code holidayStartTime}
     * @throws ValidationException if {@code holidayName} is null or blank,
     *                             or if {@code holidayEndTime} is before {@code holidayStartTime}
     */
    public HolidayOrderService(String serviceName,
                               String holidayName,
                               LocalDateTime holidayStartTime,
                               LocalDateTime holidayEndTime) {
        // always use 10% discount for holiday service
        super(serviceName, 0.10);

        if (holidayName == null || holidayName.isBlank()) {
            throw new ValidationException("Holiday name must not be blank.");
        }
        if (holidayEndTime.isBefore(holidayStartTime)) {
            throw new ValidationException("Holiday end time must be after start time.");
        }
        this.holidayName      = holidayName.trim();
        this.holidayStartTime = holidayStartTime;
        this.holidayEndTime   = holidayEndTime;
    }

    /**
     * Checks whether the given order falls within the holiday period.
     *
     * @param order the order to test; must not be null
     * @return {@code true} if {@code order.getOrderTime()} is between
     *         {@code holidayStartTime} and {@code holidayEndTime}, inclusive; {@code false} otherwise
     * @throws ValidationException if {@code order} is null
     */
    public boolean isHolidayApplicable(Order order) {
        if (order == null) {
            throw new ValidationException("Order must not be null.");
        }
        LocalDateTime orderTime = order.getOrderTime();
        return !orderTime.isBefore(holidayStartTime) &&
                !orderTime.isAfter(holidayEndTime);
    }

    /**
     * Applies this holiday service to the given order.
     * If the order falls within the holiday window, a 10% discount
     * will be applied when computing the final price.
     *
     * @param order the order to apply service to; must not be null
     * @throws ValidationException if {@code order} is null
     */
    @Override
    public void applyToOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order must not be null");
        }
        order.setOrderService(this);
    }

    /**
     * @return the configured holiday name
     */
    public String getHolidayName() {
        return holidayName;
    }

    /**
     * @return inclusive start of the holiday period
     */
    public LocalDateTime getHolidayStartTime() {
        return holidayStartTime;
    }

    /**
     * @return inclusive end of the holiday period
     */
    public LocalDateTime getHolidayEndTime() {
        return holidayEndTime;
    }
}
