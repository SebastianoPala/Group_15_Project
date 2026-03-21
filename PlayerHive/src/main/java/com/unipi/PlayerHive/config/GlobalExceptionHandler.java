package com.unipi.PlayerHive.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.MethodNotAllowedException;

import java.util.Map;
import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> generalExceptionHandler(Exception e) {
     ///   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred in the server");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.toString()); // TODO REMOVE, left for debug
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> noSuchElementExceptionHandler(NoSuchElementException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("The method is not allowed");
    }


    /*

    @ExceptionHandler()
    public ResponseEntity<Map<String,String>>

    @ExceptionHandler()
    public ResponseEntity<Map<String,String>>

    @ExceptionHandler()
    public ResponseEntity<Map<String,String>>

    */
}
