package tn.esprit.eventsproject.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@RestControllerAdvice(assignableTypes = TacheRestController.class)
public class TacheExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> missing(EntityNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> invalid(Exception ex) {
        log.warn("Role request rejected reason=invalid_input");
        return error(HttpStatus.BAD_REQUEST, "Invalid participant id or role");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> unexpected(Exception ex) {
        // Log only the exception type: JDBC messages may contain connection details.
        log.error("Role operation failed exceptionType={}", ex.getClass().getSimpleName());
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Role operation failed");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Collections.singletonMap("error", message));
    }
}
