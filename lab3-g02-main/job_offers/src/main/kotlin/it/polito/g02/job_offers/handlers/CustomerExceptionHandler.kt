package it.polito.g02.job_offers.handlers

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class CustomerHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(CustomerNotFoundException :: class)
    fun handlerCustomerNotFoundException (e: CustomerNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(BadCustomerRequestException :: class)
    fun handlerBadCustomerRequestException (e: BadCustomerRequestException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)

    @ExceptionHandler(UpdateCustomerException :: class)
    fun handlerUpdateCustomerException (e: UpdateCustomerException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)

    @ExceptionHandler(NoteNotFoundException :: class)
    fun handlerNoteNotFoundException (e: NoteNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)
}

class CustomerNotFoundException(message: String) : Throwable(message)
class BadCustomerRequestException(message: String) : Exception(message)
class UpdateCustomerException(message: String) : Exception(message)
class NoteNotFoundException(message: String) : Exception(message)