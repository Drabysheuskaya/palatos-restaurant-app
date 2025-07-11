// src/main/java/com/darina/PalaTOS/repository/ImageRepository.java
package com.danven.web_library.repository;

import com.danven.web_library.domain.product.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository interface for accessing and managing Image entities.
 */
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Deletes all images associated with the given product ID.
     *
     * @param productId The ID of the product whose images are to be removed.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Image i WHERE i.product.id = :productId")
    void deleteImagesByProductId(@Param("productId") Long productId);

    List<Image> findAllByProductId(Long productId);

}
