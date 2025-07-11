package com.danven.web_library.domain.product;

import java.util.Locale;

public enum ImageFormat {

    PNG,JPG,JPEG;

    public static ImageFormat fromContentType(String contentType) {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is null");
        }
        contentType = contentType.toLowerCase(Locale.ROOT);
        return switch (contentType) {
            case "image/png"          -> PNG;
            case "image/jpeg", "image/jpg" -> JPEG;
            default -> throw new IllegalArgumentException("Unsupported image type: " + contentType);
        };
    }
}
