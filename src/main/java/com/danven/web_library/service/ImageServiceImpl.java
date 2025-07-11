// src/main/java/com/darina/PalaTOS/service/ImageServiceImpl.java
package com.danven.web_library.service;

import com.danven.web_library.domain.product.Image;
import com.danven.web_library.domain.product.ImageFormat;
import com.danven.web_library.dto.ImageDto;
import com.danven.web_library.exceptions.ValidationException;
import com.danven.web_library.repository.ImageRepository;
import com.danven.web_library.repository.ProductRepository;
import com.danven.web_library.util.ImageUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing product images.
 * Provides methods to list, upload, convert to DTO and delete images
 * associated with products.
 */
@Service
@Transactional
public class ImageServiceImpl implements ImageService {

    private final ImageRepository   imageRepo;
    private final ProductRepository productRepo;

    public ImageServiceImpl(ImageRepository imageRepo,
                            ProductRepository productRepo) {
        this.imageRepo   = imageRepo;
        this.productRepo = productRepo;
    }

    /**
     * Retrieves all images for the given product and converts them to DTOs.
     *
     * @param productId the ID of the product whose images to list
     * @return a list of {@link ImageDto} representing each image
     */
    @Override
    public List<ImageDto> findAllByProduct(Long productId) {
        return imageRepo.findAllByProductId(productId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Uploads a new image file for the specified product.
     * Reads the bytes from the {@link MultipartFile}, determines the image format,
     * creates and saves a new {@link Image} entity, and returns its DTO.
     *
     * @param productId the ID of the product to attach the image to
     * @param file      the uploaded image file
     * @param preview   whether the image should be marked as the product’s preview image
     * @return the saved {@link ImageDto}
     * @throws ValidationException if the product does not exist or file reading fails
     */
    @Override
    public ImageDto uploadImage(Long productId, MultipartFile file, boolean preview) {
        var product = productRepo.findById(productId)
                .orElseThrow(() -> new ValidationException("Unknown product ID " + productId));

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new ValidationException("Failed to read image bytes");
        }

        var img = new Image(
                data,
                ImageFormat.fromContentType(file.getContentType()),
                preview,
                product
        );

        imageRepo.save(img);
        return toDto(img);
    }

    /**
     * Deletes the image with the specified ID.
     *
     * @param imageId the ID of the image to delete
     * @throws ValidationException if no image exists with the given ID
     */
    @Override
    public void deleteImage(Long imageId) {
        if (!imageRepo.existsById(imageId)) {
            throw new ValidationException("No image with ID " + imageId);
        }
        imageRepo.deleteById(imageId);
    }


    /**
     * Converts an {@link Image} entity to its corresponding {@link ImageDto}.
     * Encodes the raw bytes to a Base64 data URI via {@link ImageUtil}.
     *
     * @param img the Image entity to convert
     * @return the resulting {@link ImageDto}
     */
    private ImageDto toDto(Image img) {
        return new ImageDto(
                img.getId(),
                img.isPreview(),
                img.getFormat().name(),             // pass the enum name (e.g. "PNG")
                ImageUtil.getImgData(img.getImage())
        );
    }

}
