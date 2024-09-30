# Dynamo Image: Image Size Conversion Using Imgscalr


## Overview

Dynamo Image is a module within the Dynamo ecosystem that focuses on resizing and compressing images to specific dimensions or target file sizes. This is achieved using the `imgscalr` library, which is designed for fast and high-quality image scaling in Java. The module supports various compression configurations to meet different image optimization needs, including specifying target sizes, widths, or heights for compressed images.

## Prerequisites

- **Java 17.x**
- **Maven 3.8.x**

## Dependencies

- **imgscalr-lib**: A simple and efficient image-scaling library in Java.
- **dynamo-utils**: Utility functions and helpers for Dynamo.

## Configuration Properties

- **`dynamo.image.compression.quality.max`**:  - Maximum quality setting for image compression.
- **`dynamo.image.compression.quality.min`**:  - Minimum quality setting for image compression.
- **`dynamo.image.compression.quality.step`**:  - Step size for adjusting the quality during compression.
- **`dynamo.image.compression.format`**:  - Default format for compressed images (e.g., jpeg, png).

## Vocabulary/Terminology

- **Compression**: The process of reducing the size of an image file while maintaining acceptable quality.
- **Image Scaling**: Adjusting the dimensions (width, height) of an image while maintaining its aspect ratio.
- **Target Size**: The desired size in bytes for a compressed image.
- **Target Width/Height**: The desired width or height (in pixels) for a resized image.

## Technologies/3rd Party Libraries

- **imgscalr**: A simple image-scaling library that provides fast and high-quality resizing of images.
- **Dynamo Utils**: A set of utility classes and methods specific to the Dynamo framework.

## Sample Use Cases

- **Image Compression**: Compress images to meet specific size requirements without compromising much on quality.
- **Image Resizing**: Adjust images to a target width or height to fit design or storage constraints.
- **Optimizing Image Uploads**: Ensure that images uploaded by users are compressed to specific sizes for performance improvements.

## Service

### ImageService

- **`compressImageWithTargetSize(byte[] inputImageBytes, int targetSizeInBytes)`**
  - **Description**: Compresses the input image to achieve a target file size.
  - **Parameters**:
    - `inputImageBytes`: The original image data as a byte array.
    - `targetSizeInBytes`: The target size for the compressed image.
  - **Returns**: A byte array containing the compressed image.
  - **Throws**: `DynamoImageException` if an error occurs during the compression process.

- **`compressImageWithTargetSizeAndWidth(byte[] inputImageBytes, int targetSizeInBytes, int targetWidthInPixels)`**
  - **Description**: Compresses the input image to a target size and scales it to the specified width.
  - **Parameters**:
    - `inputImageBytes`: The original image data as a byte array.
    - `targetSizeInBytes`: The target size for the compressed image.
    - `targetWidthInPixels`: The target width in pixels for the scaled image.
  - **Returns**: A byte array containing the compressed image resized to the specified width.
  - **Throws**: `DynamoImageException` if an error occurs during the compression or resizing process.

- **`compressImageWithTargetSizeAndHeight(byte[] inputImageBytes, int targetSizeInBytes, int targetHeightInPixels)`**
  - **Description**: Compresses the input image to a target size and scales it to the specified height.
  - **Parameters**:
    - `inputImageBytes`: The original image data as a byte array.
    - `targetSizeInBytes`: The target size for the compressed image.
    - `targetHeightInPixels`: The target height in pixels for the scaled image.
  - **Returns**: A byte array containing the compressed image resized to the specified height.
  - **Throws**: `DynamoImageException` if an error occurs during the compression or resizing process.

## Summary

Dynamo Image provides a comprehensive solution for compressing and resizing images in Java using the `imgscalr` library. By supporting dynamic configuration properties, the module allows flexibility in how images are compressed and resized. This is particularly useful for optimizing image storage and handling, ensuring that images meet specific size or dimension requirements for web applications or other use cases. The service methods allow developers to compress images based on file size, width, or height while maintaining quality, making it an essential tool for efficient image management in Dynamo applications.

