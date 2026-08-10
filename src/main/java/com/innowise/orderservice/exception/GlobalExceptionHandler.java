package com.innowise.orderservice.exception;

import com.innowise.orderservice.dto.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentException(MethodArgumentNotValidException e) {
    int code = HttpStatus.BAD_REQUEST.value();
    String message = e.getMessage();
    LocalDateTime now = LocalDateTime.now();
    ErrorResponse errorResponse = new ErrorResponse(code, message, now);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityException(DataIntegrityViolationException e) {
    int code = HttpStatus.CONFLICT.value();
    String message = e.getMessage();
    LocalDateTime now = LocalDateTime.now();
    ErrorResponse errorResponse = new ErrorResponse(code, message, now);
    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
    int code = HttpStatus.NOT_FOUND.value();
    String message = e.getMessage();
    LocalDateTime now = LocalDateTime.now();
    ErrorResponse errorResponse = new ErrorResponse(code, message, now);
    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UserServiceException.class)
  public ResponseEntity<ErrorResponse> handleException(UserServiceException e) {
    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
    String message = e.getMessage();
    LocalDateTime now = LocalDateTime.now();
    ErrorResponse errorResponse = new ErrorResponse(code, message, now);
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception exception) {
    int code = HttpStatus.INTERNAL_SERVER_ERROR.value();
    String message = exception.getMessage();
    LocalDateTime now = LocalDateTime.now();
    ErrorResponse errorResponse = new ErrorResponse(code, message, now);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}