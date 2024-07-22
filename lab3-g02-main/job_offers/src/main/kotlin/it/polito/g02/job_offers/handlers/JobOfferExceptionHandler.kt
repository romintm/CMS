package it.polito.g02.job_offers.handlers

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MessageExceptionHandler {
    @ExceptionHandler(JobOfferNotFoundException::class)
    fun handleJobOfferNotFound(e: JobOfferNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)
    @ExceptionHandler(InvalidStateTransitionException::class)
    fun handleInvalidState(e: InvalidStateTransitionException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)
    @ExceptionHandler(ProfessionalNotAvailableException::class)
    fun handleProfessionalNotAvailable(e: ProfessionalNotAvailableException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)
    @ExceptionHandler(NoValuePresentException::class)
    fun handleNoValue(e: NoValuePresentException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message!!)
}

class JobOfferNotFoundException(message: String) : Throwable(message)
class InvalidStateTransitionException(message: String): Throwable(message)
class ProfessionalNotAvailableException(message: String): Throwable(message)
class NoValuePresentException(message: String): Throwable(message)
