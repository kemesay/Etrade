package Dx_ET_Trade.ET_Trade.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import Dx_ET_Trade.ET_Trade.exception.Syncexception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Syncexception.class)
    public ResponseEntity<ErrorResponse> handleSyncException(Syncexception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                System.currentTimeMillis());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Data
    @AllArgsConstructor
    static class ErrorResponse {
        private int status;
        private String message;
        private long timestamp;
    }
}
