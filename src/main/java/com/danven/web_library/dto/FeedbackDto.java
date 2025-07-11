package com.danven.web_library.dto;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for collecting customer feedback input.
 * Contains the feedback text and the submission timestamp
 * (which is set by the controller, not the form).
 */

public class FeedbackDto {


    /** The textual content of the feedback; must not be blank. */
    @NotBlank(message = "Feedback text must not be empty")
    private String description;

    /** Timestamp of when the feedback was submitted; set in controller. */
    private LocalDateTime submittedAt;

    /**
     * Default constructor.
     * {@code submittedAt} is populated in the controller before saving.
     */
    public FeedbackDto() {
        // submittedAt is set by controller, not here
    }

    /**
     * Constructs a FeedbackDto with the given description.
     *
     * @param description the feedback text; must not be blank
     */
    public FeedbackDto(String description) {
        this.description = description;
    }

    /**
     * Returns the feedback description.
     *
     * @return the feedback text
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the feedback description.
     *
     * @param description the feedback text; must not be blank
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the submission timestamp.
     *
     * @return the time when feedback was submitted
     */
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    /**
     * Sets the submission timestamp.
     *
     * @param submittedAt the time to record as submission
     */
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
