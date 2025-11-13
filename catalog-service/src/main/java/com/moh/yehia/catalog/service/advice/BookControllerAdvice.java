package com.moh.yehia.catalog.service.advice;

import com.moh.yehia.catalog.service.exception.BookAlreadyExistsException;
import com.moh.yehia.catalog.service.exception.BookNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BookControllerAdvice {
    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String bookNotFoundHandler(BookNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(BookAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    String bookAlreadyExistsHandler(BookNotFoundException e) {
        return e.getMessage();
    }
}
