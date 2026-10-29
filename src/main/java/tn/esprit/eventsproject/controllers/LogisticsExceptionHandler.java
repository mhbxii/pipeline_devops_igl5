package tn.esprit.eventsproject.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.eventsproject.services.LogisticsEventNotFoundException;

import java.util.Collections;
import java.util.Map;

@Slf4j
@RestControllerAdvice(assignableTypes = EventRestController.class)
public class LogisticsExceptionHandler {
    @ExceptionHandler(LogisticsEventNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(LogisticsEventNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalidInput(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", exception.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> persistenceFailure(DataAccessException exception) {
        log.error("Database failure while processing an event/logistics request", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("error", "Database operation failed"));
    }
}
