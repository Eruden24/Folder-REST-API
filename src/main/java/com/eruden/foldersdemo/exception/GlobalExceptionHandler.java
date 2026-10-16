package com.eruden.foldersdemo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Turns every error into the same JSON format ({@link ProblemDetail}).
 * <p>
 * Extending {@link ResponseEntityExceptionHandler} makes Spring's own errors (wrong parameter type,
 * unreadable JSON, unknown URL, ...) use that format too. Validation errors are overridden below
 * to add an {@code errors} map that says which field is wrong.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Folder doesn't exist → 404
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Your own business rules (bad parent, self-parenting, loops) → 400
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // The database refused the change, e.g. deleting a folder that still has subfolders → 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Database rejected a change: {}", ex.getMostSpecificCause().getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The change conflicts with existing data, for example a folder that still has subfolders.");
    }

    // @Valid on a request body failed → 400 with the wrong fields
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        return invalidRequest(errors, headers, status);
    }

    // A constraint on a method parameter failed (@Positive id, or @Valid body next to it) → 400
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        Map<String, String> errors = new HashMap<>();

        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                // A request body: report each wrong field
                for (FieldError error : parameterErrors.getFieldErrors()) {
                    errors.put(error.getField(), error.getDefaultMessage());
                }
            } else {
                // A simple parameter like the id in the URL: report the parameter itself
                String name = result.getMethodParameter().getParameterName();
                for (MessageSourceResolvable error : result.getResolvableErrors()) {
                    errors.put(name, error.getDefaultMessage());
                }
            }
        }

        return invalidRequest(errors, headers, status);
    }

    private ResponseEntity<Object> invalidRequest(Map<String, String> errors, HttpHeaders headers, HttpStatusCode status) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Invalid request");
        problem.setProperty("errors", errors);

        return ResponseEntity.status(status).headers(headers).body(problem);
    }
}
