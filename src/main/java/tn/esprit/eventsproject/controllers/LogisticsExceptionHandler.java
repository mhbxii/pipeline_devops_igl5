package tn.esprit.eventsproject.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tn.esprit.eventsproject.services.LogisticsEventNotFoundException;

import java.util.Collections;
import java.util.Map;

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
}
