package ru.practicum.explorewithme.stats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.explorewithme.stats.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiErrorAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.toString());
        body.put("reason", "Incorrectly made request.");
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now().format(DateTimeUtils.FORMATTER));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
