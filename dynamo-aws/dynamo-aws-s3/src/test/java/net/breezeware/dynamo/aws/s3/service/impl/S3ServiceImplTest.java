package net.breezeware.dynamo.aws.s3.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.breezeware.dynamo.aws.s3.exception.DynamoS3Exception;
import net.breezeware.dynamo.aws.s3.service.api.S3Service;

import lombok.extern.slf4j.Slf4j;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
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

@Slf4j
@ExtendWith(MockitoExtension.class)
public class S3ServiceImplTest {

    @Mock
    private static S3Client s3Client;

    @Mock
    private static S3Client errorS3Client = S3Client.builder().region(null).httpClient(null).build();

    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        s3Service = new S3ServiceImpl(s3Client);
    }

    @Test
    void itShouldUploadObject() throws IOException, DynamoS3Exception {
        log.info("Testing itShouldUploadObject()");

        // given
        String bucketName = "bucket";
        String objectKey = "object.txt";
        File uploadFile = new File("src/test/resources/test.txt");
        byte[] uploadObject = Files.readAllBytes(uploadFile.toPath());

        // when
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("1111").build());

        // then
        assertThat(s3Service.uploadObject(bucketName, objectKey, uploadObject)).isEqualTo("1111");

        log.info("Completed testing itShouldUploadObject()");
    }

    @Test
    void itShouldNotUploadAndThrowWhenBucketIsNotPresent() throws IOException, DynamoS3Exception {
        log.info("Testing itShouldNotUploadAndThrowWhenBucketIsNotPresent()");

        // given
        String bucketName = "unknown-bucket";
        String objectKey = "object.txt";
        File uploadFile = new File("src/test/resources/test.txt");
        byte[] uploadObject = Files.readAllBytes(uploadFile.toPath());
        // when
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        when(s3Client.putObject(eq(putObjectRequest), any(RequestBody.class))).thenThrow(S3Exception.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorMessage("Bucket not present").build()).build());
        assertThatThrownBy(() -> s3Service.uploadObject(bucketName, objectKey, uploadObject))
                .isInstanceOf(DynamoS3Exception.class);

        log.info("Completed testing itShouldNotUploadAndThrowWhenBucketIsNotPresent()");
    }

    @Test
    void itShouldNotUploadAndThrowWhenClientError() throws IOException, DynamoS3Exception {
        log.info("Testing itShouldNotUploadAndThrowWhenClientError()");

        // given
        String bucketName = "bucket";
        String objectKey = "object.txt";
        File uploadFile = new File("src/test/resources/test.txt");
        byte[] uploadObject = Files.readAllBytes(uploadFile.toPath());
        setupS3Service(errorS3Client);
        // when
        PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        when(errorS3Client.putObject(eq(putObjectRequest), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("Invalid sdk client"));
        // then
        assertThatThrownBy(() -> s3Service.uploadObject(bucketName, objectKey, uploadObject))
                .isInstanceOf(DynamoS3Exception.class);

        log.info("Completed testing itShouldNotUploadAndThrowWhenClientError()");
    }

    private void setupS3Service(S3Client s3Client) {
        this.s3Service = new S3ServiceImpl(s3Client);
    }

    @Test
    void itShouldDeleteObject() throws DynamoS3Exception {
        log.info("Testing itShouldDeleteObject()");

        // given
        String bucketName = "bucket";
        String objectKey = "object-key/object.txt";
        // when
        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        // then
        s3Service.deleteObject(bucketName, objectKey);
        verify(s3Client, times(1)).deleteObject(deleteObjectRequest);

        log.info("Completed testing itShouldDeleteObject()");
    }

    @Test
    void itShouldNotDeleteAndThrowWhenBucketIsNotPresent() {
        log.info("Testing itShouldNotDeleteAndThrowWhenBucketIsNotPresent()");

        // given
        String bucketName = "unknown-bucket";
        String objectKey = "object-key/object.txt";
        // when
        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        when(s3Client.deleteObject(deleteObjectRequest)).thenThrow(S3Exception.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorMessage("Bucket not present").build()).build());
        // then
        assertThatThrownBy(() -> s3Service.deleteObject(bucketName, objectKey)).isInstanceOf(DynamoS3Exception.class);

        log.info("Completed testing itShouldNotDeleteAndThrowWhenBucketIsNotPresent()");
    }

    @Test
    void itShouldNotDeleteAndThrowWhenClientError() {
        log.info("Testing itShouldNotDeleteAndThrowWhenClientError()");

        // given
        String bucketName = "unknown-bucket";
        String objectKey = "object-key/object.txt";
        // when
        DeleteObjectRequest deleteObjectRequest =
                DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        setupS3Service(errorS3Client);
        when(errorS3Client.deleteObject(deleteObjectRequest)).thenThrow(SdkClientException.class);
        // then
        assertThatThrownBy(() -> s3Service.deleteObject(bucketName, objectKey)).isInstanceOf(DynamoS3Exception.class);

        log.info("Completed testing itShouldNotDeleteAndThrowWhenClientError()");
    }

    @Test
    void itShouldDownload() throws DynamoS3Exception {
        log.info("Testing itShouldDownload()");

        // given
        String bucketName = "bucket";
        String objectKey = "object-key/object.txt";
        // when
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().key(objectKey).bucket(bucketName).build();
        GetObjectResponse getObjectResponse = GetObjectResponse.builder().eTag("1111").build();
        ResponseBytes<GetObjectResponse> getObjectResponseResponseBytes =
                ResponseBytes.fromByteArray(getObjectResponse, new byte[2]);
        when(s3Client.getObjectAsBytes(getObjectRequest)).thenReturn(getObjectResponseResponseBytes);

        // then
        assertThat(s3Service.downloadObject(bucketName, objectKey)).isNotEmpty();

        log.info("Completed testing itShouldDownload()");
    }

    @Test
    void itShouldNotDownloadAndThrowsWhenNoSuchObjectKey() {
        log.info("Testing itShouldNotDownloadAndThrowsWhenNoSuchObjectKey");

        // given
        String bucketName = "bucket";
        String objectKey = "unknown-key/unknown-object.txt";
        // when
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().key(objectKey).bucket(bucketName).build();
        when(s3Client.getObjectAsBytes(getObjectRequest)).thenThrow(NoSuchKeyException.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorMessage("No object with key present").build()).build());
        // then
        assertThatThrownBy(() -> s3Service.downloadObject(bucketName, objectKey)).isInstanceOf(DynamoS3Exception.class);

        log.info("Completed testing itShouldNotDownloadAndThrowsWhenNoSuchObjectKey");
    }

    @Test
    void itShouldNotDownloadAndThrowsWhenInvalidObjectState() {
        log.info("Testing itShouldNotDownloadAndThrowsWhenInvalidObjectState");

        // given
        String bucketName = "bucket";
        String objectKey = "object-key/invalid-object-state.txt";
        // when
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().key(objectKey).bucket(bucketName).build();
        when(s3Client.getObjectAsBytes(eq(getObjectRequest))).thenThrow(InvalidObjectStateException.builder()
                .awsErrorDetails(AwsErrorDetails.builder().errorMessage("Object state is invalid").build()).build());
        // then
        assertThatThrownBy(() -> s3Service.downloadObject(bucketName, objectKey)).isInstanceOf(DynamoS3Exception.class);

        log.info("Completed testing itShouldNotDownloadAndThrowsWhenInvalidObjectState");
    }

    @Test
    void itShouldNotDownloadAndThrowsWhenClientError() {
        log.info("Testing itShouldNotDownloadAndThrowsWhenClientError");

        // given
        String bucketName = "bucket";
        String objectKey = "object-key/invalid-object-state.txt";
        // when
        GetObjectRequest getObjectRequest = GetObjectRequest.builder().key(objectKey).bucket(bucketName).build();
        when(s3Client.getObjectAsBytes(eq(getObjectRequest)))
                .thenThrow(SdkClientException.create("Invalid sdk client"));
        // then
        assertThatThrownBy(() -> s3Service.downloadObject(bucketName, objectKey)).isInstanceOf(DynamoS3Exception.class);

        log.info("Completed testing itShouldNotDownloadAndThrowsWhenClientError");
    }

}
