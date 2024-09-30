# Dynamo Utils Module Documentation


---

## Overview

The **Dynamo Utils Module** provides various utility classes designed to assist with common programming tasks across different areas. This module includes utilities for validation, bean manipulation, date and time processing, string operations, and exception handling. The utility classes included are `annotations`, `bean`, `date`, `string`, and `exception`.

## Modules and Classes

### Module: annotations

#### Class: `UuidValidator`

The `UuidValidator` class implements the `ConstraintValidator` interface to validate UUID values.

- **Validation**: Ensures that a given UUID is valid.
- **Usage**: Applied to fields or parameters to enforce UUID format constraints.

#### Annotation: `ValidUuid`

Custom annotation used to validate UUID values.

- **Default Message**: "Invalid UUID"
- **Targets**: FIELD, PARAMETER
- **Retention**: RUNTIME

### Module: bean

#### Class: `BeanUtils`

Utility class for working with Java beans.

- **`getBeansWithFields(List<T> beans, String[] requestedFields)`**: Retrieves a list of maps representing beans with only the specified fields.
- **`getBeanWithFields(T bean, String[] requestedFields)`**: Retrieves a map representing a bean with only the specified fields.
- **`hasField(Class<T> type, String requestedField)`**: Checks if a class has a specified field.

### Module: date

#### Class: `DateTimeProcessingUtils`

Utility for date and time processing.

- **`parseLocalDateTime(String data, String dateTimeFormatterPattern)`**: Parses a `LocalDateTime` from a string based on the specified formatter.
- **`parseLocalDate(String data, String dateFormatterPattern)`**: Parses a `LocalDate` from a string based on the specified formatter.

### Module: string

#### Class: `RegexUtils`

Utility for regular expression matching.

- **`matchesExact(String pattern, String data)`**: Matches data exactly with the pattern. The pattern is case-insensitive by default.
- **`matchesPartial(String pattern, String data)`**: Matches data partially with the pattern. The pattern is case-insensitive by default.

#### Class: `StringProcessingUtils`

Utility for string processing.

- **`cleanAndCapitalizeEachWord(String data)`**: Cleans and capitalizes each word in the string. Removes leading and trailing spaces and capitalizes each word.

### Module: exception

#### Class: `DynamoException`

Custom exception class for handling application-specific errors.

- **Attributes**:
    - `message`: Error message provided during exception.
    - `errorMessages`: List of error messages associated with the exception.
    - `status`: HTTP status code for the exception.
- **Constructors**:
    - `DynamoException(String message, HttpStatus status)`
    - `DynamoException(List<String> errorMessages, HttpStatus status)`

#### Class: `DynamoExceptionHandler`

Global exception handler for managing exceptions and converting them into a client-readable format.

- **Exception Handlers**:
    - `handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request)`
    - `handleDynamoException(DynamoException dynamoException)`
    - `handleNoSuchElementException(NoSuchElementException noSuchElementException)`
    - `handleIllegalArgumentException(IllegalArgumentException illegalArgumentException)`
    - `handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request)`
    - `handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request)`
    - `handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request)`
    - `handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request)`
    - `handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request)`
    - `handleMissingRequestHeaderException(MissingRequestHeaderException ex)`

#### Class: `DynamoSdkException`

Custom exception for SDK-specific errors.

- **Constructors**:
    - `DynamoSdkException()`
    - `DynamoSdkException(String message, Throwable cause)`
    - `DynamoSdkException(String message)`
    - `DynamoSdkException(Throwable cause)`

#### Class: `ErrorResponse`

Class representing the error response format.

- **Attributes**:
    - `statusCode`: HTTP status code for the exception.
    - `message`: Status code name for the exception.
    - `details`: List of error messages with brief notes.

#### Class: `ValidationExceptionUtils`

Utility for handling constraint violations.

- **`handleException(Set<ConstraintViolation<T>> fieldViolations)`**: Throws a `DynamoException` if any constraint violations are found.

---

## Summary

The **Dynamo Utils Module** is a collection of utility classes designed to simplify common programming tasks. It includes:

- **`annotations`**: For UUID validation with custom constraints.
- **`bean`**: For manipulating Java beans and their properties.
- **`date`**: For parsing date and time strings.
- **`string`**: For regular expression matching and string processing.
- **`exception`**: For handling and formatting exceptions, including custom exceptions and global exception handling.

These utilities provide essential functionality for managing and processing data efficiently, ensuring code consistency and reducing redundancy.

