package net.breezeware.dynamo.aws.s3.service.impl;

import org.springframework.stereotype.Service;

import net.breezeware.dynamo.aws.s3.exception.DynamoS3Exception;
import net.breezeware.dynamo.aws.s3.service.api.S3Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.InvalidObjectStateException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * S3 service implementation.
 */
@Service
@Slf4j
@AllArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Override
    public String uploadObject(String bucketName, String objectKey, byte[] uploadObject) throws DynamoS3Exception {
        log.info("Entering uploadObject(), bucketName = {}, objectKey = {}", bucketName, objectKey);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(objectKey).build();
            RequestBody requestBody = RequestBody.fromBytes(uploadObject);
            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, requestBody);
            String etag = putObjectResponse.eTag();

            log.info("Leaving uploadObject, etag = {}", etag);
            return etag;
        } catch (S3Exception e) {
            log.error("AWS S3 service error while uploading object, error = {}", e.awsErrorDetails().errorMessage());
            throw new DynamoS3Exception(e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while uploading object to the S3 bucket, error = {}", e.getMessage());
            throw new DynamoS3Exception(e.getMessage(), e);
        }

    }

    @Override
    public void deleteObject(String bucketName, String objectKey) throws DynamoS3Exception {
        log.debug("Entering deleteObject(), bucketName = {}, objectKey = {}", bucketName, objectKey);
        try {
            DeleteObjectRequest deleteObjectRequest =
                    DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Deleted object with key = {} from bucket = {}", objectKey, bucketName);
            log.debug("Leaving deleteObject()");
        } catch (S3Exception e) {
            log.error("AWS S3 service error while deleting object from the S3 bucket, error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoS3Exception(e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while deleting object from the S3 bucket, error = {}", e.getMessage());
            throw new DynamoS3Exception(e.getMessage(), e);
        }

    }

    @Override
    public byte[] downloadObject(String bucketName, String objectKey) throws DynamoS3Exception {
        log.debug("Entering downloadObject(), bucketName = {}, objectKey = {}", bucketName, objectKey);
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();
            ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(getObjectRequest);
            log.debug("Downloaded object as bytes. objectAsBytes = {}", objectAsBytes);
            log.debug("Leaving downloadObject()");
            return objectAsBytes.asByteArray();
        } catch (NoSuchKeyException e) {
            log.error("AWS S3 service error while downloading object from the S3 bucket, no such key, error = {}",
                    e.awsErrorDetails().errorMessage());
            throw new DynamoS3Exception(e.getMessage(), e);
        } catch (InvalidObjectStateException e) {
            log.error("""
                    AWS S3 service error while downloading object from the S3 bucket, \
                    invalid object state, error = {}\
                    """, e.awsErrorDetails().errorMessage());
            throw new DynamoS3Exception(e.getMessage(), e);
        } catch (SdkClientException e) {
            log.error("AWS SDK client error while uploading object to the S3 bucket, error = {}", e.getMessage());
            throw new DynamoS3Exception(e.getMessage(), e);
        }

    }

}
