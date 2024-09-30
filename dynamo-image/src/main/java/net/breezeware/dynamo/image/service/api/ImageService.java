package net.breezeware.dynamo.image.service.api;

import net.breezeware.dynamo.image.exception.DynamoImageException;

public interface ImageService {

    /**
     * Compresses the input image to achieve the target size in bytes.
     * @param  inputImageBytes      The input image data as a byte array.
     * @param  targetSizeInBytes    The target size in bytes for the compressed
     *                              image.
     * @return                      The compressed image data as a byte array.
     * @throws DynamoImageException If an error occurs during image compression.
     */
    byte[] compressImageWithTargetSize(byte[] inputImageBytes, int targetSizeInBytes) throws DynamoImageException;

    /**
     * Compresses the input image to achieve the target size in bytes and fit to the
     * specified width.
     * @param  inputImageBytes      The input image data as a byte array.
     * @param  targetSizeInBytes    The target size in bytes for the compressed
     *                              image.
     * @param  targetWidthInPixels  The target width in pixels for the compressed
     *                              image.
     * @return                      The compressed image data as a byte array.
     * @throws DynamoImageException If an error occurs during image compression.
     */
    byte[] compressImageWithTargetSizeAndWidth(byte[] inputImageBytes, int targetSizeInBytes, int targetWidthInPixels)
            throws DynamoImageException;

    /**
     * Compresses the input image to achieve the target size in bytes and fit to the
     * specified height.
     * @param  inputImageBytes      The input image data as a byte array.
     * @param  targetSizeInBytes    The target size in bytes for the compressed
     *                              image.
     * @param  targetHeightInPixels The target height in pixels for the compressed
     *                              image.
     * @return                      The compressed image data as a byte array.
     * @throws DynamoImageException If an error occurs during image compression.
     */
    byte[] compressImageWithTargetSizeAndHeight(byte[] inputImageBytes, int targetSizeInBytes, int targetHeightInPixels)
            throws DynamoImageException;
}
