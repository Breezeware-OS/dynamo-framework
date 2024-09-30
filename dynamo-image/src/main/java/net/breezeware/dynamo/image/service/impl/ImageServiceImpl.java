package net.breezeware.dynamo.image.service.impl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.breezeware.dynamo.image.exception.DynamoImageException;
import net.breezeware.dynamo.image.service.api.ImageService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ImageServiceImpl implements ImageService {

    @Value("${dynamo.image.compression.quality.max}")
    private float maxCompressionQuality;

    @Value("${dynamo.image.compression.quality.min}")
    private float minCompressionQuality;

    @Value("${dynamo.image.compression.quality.step}")
    private float compressionQualityStep;

    @Value("${dynamo.image.compression.format}")
    private String compressionFormat;

    @Override
    public byte[] compressImageWithTargetSize(byte[] inputImageBytes, int targetSizeInBytes)
            throws DynamoImageException {

        log.debug("Entering compressImageWithTargetSize(), inputImageBytes length = {}, targetSizeInBytes = {}",
                inputImageBytes.length, targetSizeInBytes);

        byte[] compressedImageBytes = compressImage(inputImageBytes, targetSizeInBytes, 0, 0);

        log.debug("Leaving compressImageWithTargetSize()");

        return compressedImageBytes;

    }

    @Override
    public byte[] compressImageWithTargetSizeAndWidth(byte[] inputImageBytes, int targetSizeInBytes,
            int targetWidthInPixels) throws DynamoImageException {

        log.debug("""
                Entering compressImageWithTargetSizeAndWidth(), inputImageBytes length = {}, \
                targetSizeInBytes = {}, targetWidthInPixels = {}\
                """, inputImageBytes.length, targetSizeInBytes, targetWidthInPixels);

        byte[] compressedImageBytes = compressImage(inputImageBytes, targetSizeInBytes, targetWidthInPixels, 0);

        log.debug("Leaving compressImageWithTargetSizeAndWidth()");

        return compressedImageBytes;
    }

    @Override
    public byte[] compressImageWithTargetSizeAndHeight(byte[] inputImageBytes, int targetSizeInBytes,
            int targetHeightInPixels) throws DynamoImageException {

        log.debug("""
                Entering compressImageWithTargetSizeAndHeight(), inputImageBytes length = {}, \
                targetSizeInBytes = {}, targetHeightInPixels = {}\
                """, inputImageBytes.length, targetSizeInBytes, targetHeightInPixels);

        byte[] compressedImageBytes = compressImage(inputImageBytes, targetSizeInBytes, 0, targetHeightInPixels);

        log.debug("Leaving compressImageWithTargetSizeAndHeight()");

        return compressedImageBytes;
    }

    /**
     * Compresses an image based on specified parameters.
     * @param  inputImageBytes      The byte array representing the input image.
     * @param  targetSizeInBytes    The target size (in bytes) for the compressed
     *                              image.
     * @param  targetWidthInPixels  The target width (in pixels) for resizing the
     *                              image.
     * @param  targetHeightInPixels The target height (in pixels) for resizing the
     *                              image.
     * @return                      A byte array containing the compressed image
     *                              data.
     * @throws DynamoImageException If an error occurs during image compression.
     */
    private byte[] compressImage(byte[] inputImageBytes, int targetSizeInBytes, int targetWidthInPixels,
            int targetHeightInPixels) throws DynamoImageException {

        log.debug("""
                Entering compressImage(), inputImageBytesSize = {}, targetSizeInBytes = {}, targetWidthInPixels = {}, \
                targetHeightInPixels = {}\
                """, inputImageBytes.length, targetSizeInBytes, targetWidthInPixels, targetHeightInPixels);

        if (targetSizeInBytes <= 0) {
            String errorMessage = "Invalid input parameter: targetSizeInBytes must be greater than 0. Actual value: %s"
                    .formatted(targetSizeInBytes);
            log.error(errorMessage);
            throw new DynamoImageException(errorMessage);
        }

        try {
            BufferedImage inputImage = ImageIO.read(new ByteArrayInputStream(inputImageBytes));
            inputImage = resizeImage(inputImage, targetWidthInPixels, targetHeightInPixels);

            ByteArrayOutputStream compressedOutputStream =
                    compressImageWithQualityIterative(inputImage, targetSizeInBytes);

            log.debug("Leaving compressImage()");

            return compressedOutputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error occurred while compressing image. Details: {}", e.getMessage(), e);
            throw new DynamoImageException(e.getMessage(), e);
        }

    }

    /**
     * Resizes the input image based on the specified width and height parameters.
     * @param  inputImage           The input image to be resized.
     * @param  targetWidthInPixels  The target width (in pixels) for resizing the
     *                              image.
     * @param  targetHeightInPixels The target height (in pixels) for resizing the
     *                              image.
     * @return                      The resized BufferedImage.
     */
    private BufferedImage resizeImage(BufferedImage inputImage, int targetWidthInPixels, int targetHeightInPixels) {

        log.debug("Entering resizeImage(), inputImage = {}, targetWidthInPixels = {}, targetHeightInPixels = {}",
                inputImage, targetWidthInPixels, targetHeightInPixels);

        if (targetWidthInPixels > 0) {
            inputImage = Scalr.resize(inputImage, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_WIDTH, targetWidthInPixels,
                    Scalr.OP_ANTIALIAS);
        } else if (targetHeightInPixels > 0) {
            inputImage = Scalr.resize(inputImage, Scalr.Method.QUALITY, Scalr.Mode.FIT_TO_HEIGHT, targetHeightInPixels,
                    Scalr.OP_ANTIALIAS);
        } else {
            inputImage = Scalr.resize(inputImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, inputImage.getWidth(),
                    inputImage.getHeight(), Scalr.OP_ANTIALIAS);
        }

        log.debug("Leaving resizeImage()");

        return inputImage;

    }

    /**
     * Compresses the input image iteratively with varying compression qualities
     * until the target size is met.
     * @param  inputImage           The input image to be compressed.
     * @param  targetSizeInBytes    The target size (in bytes) for the compressed
     *                              image.
     * @return                      A ByteArrayOutputStream containing the
     *                              compressed image data.
     * @throws DynamoImageException If an error occurs during image compression.
     */
    private ByteArrayOutputStream compressImageWithQualityIterative(BufferedImage inputImage, int targetSizeInBytes)
            throws DynamoImageException {

        log.debug("Entering compressImageWithQualityIterative(), inputImage = {}, targetSizeInBytes = {}", inputImage,
                targetSizeInBytes);

        if (maxCompressionQuality <= minCompressionQuality) {
            String errorMessage = """
                    Invalid compression quality range. The maximum compression quality (%.2f) must be \
                    greater than the minimum compression quality (%.2f).\
                    """.formatted(maxCompressionQuality, minCompressionQuality);
            log.error(errorMessage);
            throw new DynamoImageException(errorMessage);
        }

        ByteArrayOutputStream compressedOutputStream = new ByteArrayOutputStream();
        float currentCompressionQuality = maxCompressionQuality;

        while (currentCompressionQuality > minCompressionQuality) {

            compressedOutputStream.reset();
            compressImageWithQuality(inputImage, currentCompressionQuality, compressedOutputStream, compressionFormat);

            if (compressedOutputStream.size() <= targetSizeInBytes) {
                log.debug("Image compressed successfully. Size: {} bytes, Width: {} pixels, Height: {} pixels",
                        compressedOutputStream.size(), inputImage.getWidth(), inputImage.getHeight());
                return compressedOutputStream;
            }

            currentCompressionQuality -= compressionQualityStep;
        }

        log.debug("Leaving compressImageWithQualityIterative()");

        return compressedOutputStream;
    }

    /**
     * Compresses the input image with the specified compression quality.
     * @param  inputImage           The input image to be compressed.
     * @param  compressionQuality   The compression quality to be applied (between
     *                              0.1 and 1.0).
     * @param  outputStream         The output stream to which the compressed image
     *                              data will be written.
     * @throws DynamoImageException If an error occurs during image compression.
     */
    private void compressImageWithQuality(BufferedImage inputImage, float compressionQuality, OutputStream outputStream,
            String formatName) throws DynamoImageException {

        log.debug("Entering compressImageWithQuality(), inputImage = {}, compressionQuality = {}", inputImage,
                compressionQuality);

        if (inputImage == null || compressionQuality < 0.1f || compressionQuality > 1.0f) {
            String errorMessage = """
                    Invalid input parameters for image compression. \
                    inputImage: %s, compressionQuality: %s\
                    """.formatted(inputImage, compressionQuality);
            log.error(errorMessage);
            throw new DynamoImageException(errorMessage);
        }

        BufferedImage rgbImage = convertToRgb(inputImage);
        ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(formatName).next();

        ImageWriteParam writeParam = imageWriter.getDefaultWriteParam();
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        writeParam.setCompressionQuality(compressionQuality);

        try {
            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);

            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(null, new javax.imageio.IIOImage(rgbImage, null, null), writeParam);

            imageWriter.dispose();
            imageOutputStream.close();

        } catch (IOException e) {
            log.error("Error occurred while compressing image. Details: {}", e.getMessage(), e);
            throw new DynamoImageException(e.getMessage(), e);
        }

        log.debug("Leaving compressImageWithQuality()");
    }

    /**
     * Converts the input {@link BufferedImage} to the RGB color space. If the input
     * image is already in the RGB color space, the same image is returned. If the
     * input image has a different color space, it is converted to RGB.
     * @param  inputImage The original {@link BufferedImage} to be converted.
     * @return            A new {@link BufferedImage} in the RGB color space.
     */
    private BufferedImage convertToRgb(BufferedImage inputImage) {
        log.debug("Entering convertToRgb()");
        BufferedImage rgbImage =
                new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgbImage.createGraphics();
        g.drawImage(inputImage, 0, 0, null);
        g.dispose();
        log.debug("Leaving convertToRgb()");
        return rgbImage;
    }
}
