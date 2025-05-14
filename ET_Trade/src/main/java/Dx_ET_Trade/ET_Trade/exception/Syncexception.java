package Dx_ET_Trade.ET_Trade.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

public class Syncexception extends RuntimeException {
    public Syncexception(String message) {
        super(message);
    }
}

