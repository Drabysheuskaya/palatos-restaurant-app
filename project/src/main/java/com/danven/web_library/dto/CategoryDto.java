// src/main/java/com/darina/PalaTOS/dto/CategoryDto.java
package com.danven.web_library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Data Transfer Object for Category entities.
 * Used to expose category ID and name in API responses or views
 * without exposing the full domain model.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    /** Unique identifier of the category. */
    private Long id;
    /** Human-readable name of the category. */
    private String name;
}
