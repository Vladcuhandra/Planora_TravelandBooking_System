package project.planora_travelandbooking_system.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.planora_travelandbooking_system.exceptions.InvalidPasswordException;
import project.planora_travelandbooking_system.exceptions.UserAlreadyExistsException;
import project.planora_travelandbooking_system.exceptions.DuplicateTripBookingTypeException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserExists(
            UserAlreadyExistsException ex) {

        return ResponseEntity.status(HttpStatus.CONFLICT) // Status 409
                .body(Map.of(
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<?> handleInvalidPassword(InvalidPasswordException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("message", ex.getMessage())
        );
    }
    @ExceptionHandler(DuplicateTripBookingTypeException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateTripBookingType(DuplicateTripBookingTypeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }
}
