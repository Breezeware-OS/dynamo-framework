package net.breezeware.dynamo.csv.exception;

/**
 * User-defined exception based on {@link DynamoCsvException} to handle
 * exceptions during CSV file write operation.
 */
public class CsvWriteException extends DynamoCsvException {
    public CsvWriteException(String message) {
        super(message);
    }
}
