package net.breezeware.dynamo.aws.s3.service.api;

import net.breezeware.dynamo.aws.s3.exception.DynamoS3Exception;

/**
 * S3 service.
 */
public interface S3Service {

    /**
     * Uploads object to the Amazon S3.
     * @param  bucketName        Name of the bucket.
     * @param  objectKey         Uniquely identifies the object.
     * @param  uploadObject      object to be uploaded to the AWS S3 bucket.
     * @return                   Server-side ETag value for the newly created
     *                           object.
     * @throws DynamoS3Exception Throws user defined exception.
     */
    String uploadObject(String bucketName, String objectKey, byte[] uploadObject) throws DynamoS3Exception;

    /**
     * Deletes object from the Amazon S3.
     * @param  bucketName        Name of the bucket.
     * @param  objectKey         Uniquely identifies the object.
     * @throws DynamoS3Exception Throws user defined exception.
     */
    void deleteObject(String bucketName, String objectKey) throws DynamoS3Exception;

    /**
     * Downloads object from the Amazon S3 as byte array.
     * @param  bucketName        Name of the bucket.
     * @param  objectKey         Uniquely identifies the object.
     * @return                   Downloaded object as byte array.
     * @throws DynamoS3Exception in case of any error while downloading ht object
     *                           from the object.
     */
    byte[] downloadObject(String bucketName, String objectKey) throws DynamoS3Exception;
}
