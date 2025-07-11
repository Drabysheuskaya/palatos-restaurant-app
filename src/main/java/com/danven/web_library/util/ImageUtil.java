package com.danven.web_library.util;

import java.util.Base64;

/**
 * Utility class for image-related operations, including conversion of byte array to Base64 string.
 */
public class ImageUtil {

    /**
     * Converts a byte array representing image data into a Base64 encoded string.
     *
     * @param byteData The byte array containing image data.
     * @return A Base64 encoded string representation of the image data.
     */
    public static String getImgData(byte[] byteData) {
        return Base64.getEncoder().encodeToString(byteData);
    }
}

