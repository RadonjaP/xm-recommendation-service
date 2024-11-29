package com.rprelevic.xm.recom.adtin.rest;

import com.rprelevic.xm.recom.impl.ex.SymbolNotSupportedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.format.DateTimeParseException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<String> handleDateTimeParseException(DateTimeParseException ex, WebRequest request) {
        return new ResponseEntity<>("Invalid date format. Please use dd-MM-yyyy", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SymbolNotSupportedException.class)
    public ResponseEntity<String> handleSymbolNotSupportedException(SymbolNotSupportedException ex, WebRequest request) {
        return new ResponseEntity<>("Symbol not supported.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex, WebRequest request) {
        return new ResponseEntity<>("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}