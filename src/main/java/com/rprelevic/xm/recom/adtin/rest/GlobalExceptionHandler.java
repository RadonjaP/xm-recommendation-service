package com.rprelevic.xm.recom.adtin.rest;

import com.rprelevic.xm.recom.impl.ex.SymbolNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.format.DateTimeParseException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<String> handleDateTimeParseException(DateTimeParseException ex, WebRequest request) {
        LOGGER.error("DateTimeParseException: Invalid date format", ex);
        return new ResponseEntity<>("Invalid date format. Please use dd-MM-yyyy", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SymbolNotSupportedException.class)
    public ResponseEntity<String> handleSymbolNotSupportedException(SymbolNotSupportedException ex, WebRequest request) {
        LOGGER.error("SymbolNotSupportedException: Symbol not supported", ex);
        return new ResponseEntity<>("Symbol not supported.", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex, WebRequest request) {
        LOGGER.error("Exception: An unexpected error occurred", ex);
        return new ResponseEntity<>("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}