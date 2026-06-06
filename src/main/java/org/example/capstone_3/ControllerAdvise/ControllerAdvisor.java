package org.example.capstone_3.ControllerAdvise;


import org.example.capstone_3.AI.AiException;
import org.example.capstone_3.Api.ApiException;
import org.example.capstone_3.Api.ApiResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ControllerAdvisor {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = "Validation failed.";
        if (ex.getFieldError() != null && ex.getFieldError().getDefaultMessage() != null) {
            message = ex.getFieldError().getDefaultMessage();
        }
        return ResponseEntity.badRequest().body(new ApiResponse(message));
    }

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<?> handleAPIException(ApiException ex) {
        return ResponseEntity.status(400).body(new ApiResponse(ex.getMessage()));
    }

    @ExceptionHandler(AiException.class)
    public ResponseEntity<?> handleAiException(AiException ex) {
        return ResponseEntity.status(503).body(new ApiResponse(ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(400)
                .body(new ApiResponse("Missing required parameter: " + ex.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(400)
                .body(new ApiResponse("Invalid value for parameter: " + ex.getName()));
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(400).body(new ApiResponse("Invalid request body."));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(BindException ex) {
        String message = "Validation failed.";
        if (ex.getFieldError() != null && ex.getFieldError().getDefaultMessage() != null) {
            message = ex.getFieldError().getDefaultMessage();
        }
        return ResponseEntity.badRequest().body(new ApiResponse(message));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<?> handleMissingPathVariable(MissingPathVariableException ex) {
        return ResponseEntity.status(400)
                .body(new ApiResponse("Missing path variable: " + ex.getVariableName()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(405).body(new ApiResponse("Method not allowed for this URL."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity.status(415).body(new ApiResponse("Unsupported Content-Type."));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<?> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        return ResponseEntity.status(406).body(new ApiResponse("Requested response type is not supported."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFound(NoResourceFoundException ex) {
        return ResponseEntity.status(404).body(new ApiResponse("URL not found."));
    }

    @ExceptionHandler(value = DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        return ResponseEntity.status(400)
                .body(new ApiResponse("This value conflicts with existing data."));
    }

    @ExceptionHandler(value = ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<?> handleObjectOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(400)
                .body(new ApiResponse("Record was updated by another request. Please try again."));
    }

    @ExceptionHandler(value = DataAccessException.class)
    public ResponseEntity<?> handleDataAccessException(DataAccessException ex) {
        return ResponseEntity.status(400).body(new ApiResponse("Database request failed."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        return ResponseEntity.status(500).body(new ApiResponse("Unexpected server error."));
    }
}