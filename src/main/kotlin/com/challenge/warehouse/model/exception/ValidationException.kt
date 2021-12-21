package com.challenge.warehouse.model.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class ValidationException(message: String) : RuntimeException(message)

data class ErrorData(val httpStatus: HttpStatus, val message: String)
