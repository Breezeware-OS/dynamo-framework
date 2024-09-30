package net.breezeware.dynamo.utils.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Helps developer to handle exceptions across the whole application. /** Note
 * :- The class (ResponseEntityExceptionHandler) reports the class
 * HttpStatusCode and not HttpStatus
 */

@ControllerAdvice
public class DynamoExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> details = new ArrayList<>();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(status.value());
        errorResponse.setMessage(HttpStatus.valueOf(status.value()).name());
        details.add(ex.getMessage());
        // errorResponse.setDetails(List.of("Error while processing file. Maximum upload
        // file-size exceeded"));
        errorResponse.setDetails(details);

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Method invokes when the DynamoException (user-defined exception) was
     * thrown.Helps in converting DynamoException(user-defined exception) messages
     * into ErrorResponse(client-readable).
     * @param  dynamoException user-defined exception.
     * @return                 Provides the client with appropriate error messages.
     */
    @ExceptionHandler(DynamoException.class)
    public ResponseEntity<ErrorResponse> handleDynamoException(DynamoException dynamoException) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(dynamoException.getStatus().value());
        errorResponse.setMessage(dynamoException.getStatus().name());
        String message = dynamoException.getMessage();
        List<String> errorMessages = dynamoException.getErrorMessages();
        if (Objects.isNull(message) && !errorMessages.isEmpty()) {
            errorResponse.setDetails(errorMessages);
        } else {
            errorResponse.setDetails(List.of(message));
        }

        return new ResponseEntity<>(errorResponse, dynamoException.getStatus());
    }

    /**
     * Exception handler method to handle {@link NoSuchElementException}.<br>
     * Converts the error message into an {@link ErrorResponse} standard error
     * response payload.
     * @param  noSuchElementException The {@link NoSuchElementException} that was
     *                                thrown
     * @return                        An {@link ErrorResponse} containing the
     *                                standard error response payload
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException noSuchElementException) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage(HttpStatus.NOT_FOUND.name());
        errorResponse.setDetails(List.of(noSuchElementException.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Exception handler method to handle {@link IllegalArgumentException}.<br>
     * Converts the error message into an {@link ErrorResponse} standard error
     * response payload.
     * @param  illegalArgumentException The {@link IllegalArgumentException} that
     *                                  was thrown
     * @return                          An {@link ErrorResponse} containing the
     *                                  standard error response payload
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse>
            handleIllegalArgumentException(IllegalArgumentException illegalArgumentException) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage(HttpStatus.BAD_REQUEST.name());
        errorResponse.setDetails(List.of(illegalArgumentException.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> details = new ArrayList<>();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(status.value());
        errorResponse.setMessage(HttpStatus.valueOf(status.value()).name());
        details.add(ex.getMessage());
        errorResponse.setDetails(details);
        return new ResponseEntity<>(errorResponse, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> details = new ArrayList<>();
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(status.value());
        errorResponse.setMessage(HttpStatus.valueOf(status.value()).name());
        details.add(ex.getMessage().split(":")[0]);
        errorResponse.setDetails(details);

        return new ResponseEntity<>(errorResponse, status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> details = new ArrayList<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            details.add(error.getDefaultMessage());
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(status.value());
        errorResponse.setMessage(HttpStatus.valueOf(status.value()).name());
        errorResponse.setDetails(details);

        return new ResponseEntity<>(errorResponse, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> details = new ArrayList<>();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(status.value());
        errorResponse.setMessage(HttpStatus.valueOf(status.value()).name());
        details.add(ex.getLocalizedMessage().split(":")[0]);
        errorResponse.setDetails(details);
        return new ResponseEntity<>(errorResponse, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> details = new ArrayList<>();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(status.value());
        errorResponse.setMessage(HttpStatus.valueOf(status.value()).name());
        details.add(ex.getLocalizedMessage().split(":")[0]);
        errorResponse.setDetails(details);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Invokes when a request header expected in the method parameters of
     * an @RequestMapping method is not present.
     * @param  ex {@link MissingRequestHeaderException}
     * @return    Provides the client with appropriate error messages.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        List<String> details = new ArrayList<>();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage(HttpStatus.BAD_REQUEST.name());
        details.add(ex.getLocalizedMessage().split(":")[0]);
        errorResponse.setDetails(details);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
