package com.danven.web_library.domain.product;

import com.danven.web_library.exceptions.ValidationException;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a binary image associated with a {@link Product}.
 * <p>
 * Stores image data, tracks its format, and whether it is
 * used as a preview. Manages a bidirectional relationship to Product,
 * ensuring correct linking and unlinking behavior.
 */
@Entity
@Table(name = "IMAGE")
public class Image implements Serializable {

    /**
     * Primary key for the Image entity.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    /**
     * Raw binary data of the image. Cannot be null.
     */
    @NotNull(message = "Image data must not be null.")
    @Lob
    @Column(name = "image", nullable = false)
    private byte[] image;

    /**
     * Format of the image (e.g., PNG, JPEG). Cannot be null.
     */
    @NotNull(message = "Image format must not be null.")
    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private ImageFormat format;

    /**
     * Flag indicating if this image serves as a preview thumbnail.
     */
    @Column(name = "is_preview", nullable = false)
    private boolean isPreview;

    /**
     * Owning product. Cannot be null after assignment and is updatable only
     * via {@link #setProduct(Product)}.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    /**
     * Default constructor for Image.
     */
    public Image() {
    }

    /**
     * Constructs a new Image linked to a product.
     *
     * @param image     the binary image data; must not be null
     * @param format    the enum format of the image; must not be null
     * @param isPreview true if this image is a preview thumbnail
     * @param product   the product to associate; must not be null
     * @throws ValidationException if linking to a different product is attempted
     */
    public Image(byte[] image, ImageFormat format, boolean isPreview, Product product) {
        this.image = image;
        this.format = format;
        this.isPreview = isPreview;
        setProduct(product);
    }

    /**
     * Sets the product associated with the image.
     *
     * @param product the product to set.
     * @throws ValidationException if the owner of the image is being changed.
     */
    public void setProduct(Product product) {
        if (product == null) {
            Product old = this.product;
            this.product = null;
            if (old != null) {
                old.removeImage(this);
            }
            return;
        }

        // reject any attempt to reassign to a different non-null product
        if (this.product != null && this.product != product) {
            throw new ValidationException("Cannot reassign image to another product");
        }

        // normal link case
        this.product = product;
        if (!product.getImages().contains(this)) {
            product.addImage(this);
        }
    }

    /**
     * Gets the ID of the image.
     *
     * @return the image ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the byte array representing the image.
     *
     * @return the image byte array.
     */
    public byte[] getImage() {
        return image;
    }

    /**
     * Sets the byte array representing the image.
     *
     * @param image the image byte array to set.
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * Gets the format of the image.
     *
     * @return the image format.
     */
    public ImageFormat getFormat() {
        return format;
    }

    /**
     * Sets the format of the image.
     *
     * @param format the image format to set.
     */
    public void setFormat(ImageFormat format) {
        this.format = format;
    }

    /**
     * Checks if the image is a preview image.
     *
     * @return true if the image is a preview image, false otherwise.
     */
    public boolean isPreview() {
        return isPreview;
    }

    /**
     * Sets whether the image is a preview image.
     *
     * @param preview true to set the image as a preview image, false otherwise.
     */
    public void setPreview(boolean preview) {
        isPreview = preview;
    }

    /**
     * Gets the product associated with the image.
     *
     * @return the associated product.
     */
    public Product getProduct() {
        return product;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Image)) return false;
        Image that = (Image) o;
        return isPreview == that.isPreview &&
                Objects.equals(id, that.id) &&
                Arrays.equals(image, that.image) &&
                format == that.format &&
                Objects.equals(product, that.product);
    }

    @Override
    public int hashCode() {
        int h = Objects.hash(id, format, isPreview, product);
        return 31*h + Arrays.hashCode(image);
    }

}
