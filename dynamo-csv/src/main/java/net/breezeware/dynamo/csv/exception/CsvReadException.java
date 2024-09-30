package net.breezeware.dynamo.csv.exception;

/**
 * User-defined exception based on {@link DynamoCsvException} to handle
 * exceptions during CSV file read operation.
 */
public class CsvReadException extends DynamoCsvException {

    public CsvReadException(String message) {
        super(message);
    }
}
