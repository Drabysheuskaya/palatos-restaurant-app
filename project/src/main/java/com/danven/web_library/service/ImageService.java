// src/main/java/com/darina/PalaTOS/service/ImageService.java
package com.danven.web_library.service;

import com.danven.web_library.dto.ImageDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for handling Image-related operations.
 */
public interface ImageService {

    /**
     * Return all images belonging to a given product.
     */
    List<ImageDto> findAllByProduct(Long productId);

    /**
     * Upload a new image for the given product.
     *
     * @param productId the product to attach to
     * @param file      the multipart file to upload
     * @param preview   whether this should be marked as the “preview” image
     * @return the DTO of the newly created image
     */
    ImageDto uploadImage(Long productId, MultipartFile file, boolean preview);

    /**
     * Delete an image by its ID.
     */
    void deleteImage(Long imageId);
}
