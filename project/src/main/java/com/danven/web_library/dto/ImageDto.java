
package com.danven.web_library.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Data Transfer Object representing an image associated with a product.
 * Contains metadata (ID, preview flag, format) and the image content
 * as a Base64-encoded string (or URL).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private Long      id;
    private boolean   isPreview;
    private String  format;
    private String    base64Data;
}
