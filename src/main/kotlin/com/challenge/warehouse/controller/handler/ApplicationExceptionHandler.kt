package com.challenge.warehouse.controller.handler

import com.challenge.warehouse.model.exception.ErrorData
import com.challenge.warehouse.model.exception.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler


@ControllerAdvice
class ApplicationExceptionHandler {

    @ExceptionHandler(ValidationException::class)
    fun handleException(e: ValidationException) =
        with(ErrorData(HttpStatus.BAD_REQUEST, e.localizedMessage)) {
            ResponseEntity<ErrorData>(this, httpStatus)
        }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleException(e: IllegalArgumentException) =
        with(ErrorData(HttpStatus.BAD_REQUEST, e.localizedMessage)) {
            ResponseEntity<ErrorData>(this, httpStatus)
        }
}
