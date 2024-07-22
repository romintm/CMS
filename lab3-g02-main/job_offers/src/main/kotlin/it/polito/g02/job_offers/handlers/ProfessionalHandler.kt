package it.polito.g02.job_offers.handlers

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ProfessionalHandler:ResponseEntityExceptionHandler() {

    @ExceptionHandler(ProfessionalNotFoundException :: class)
    fun handleProfessionalNotFound(e:ProfessionalNotFoundException) =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.message!!)


    @ExceptionHandler(BadProfessionalRequestException::class)
    fun handleProfessionalBadRequest(e:BadProfessionalRequestException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.message!!)

    @ExceptionHandler(UpdateProfessionalException::class)
    fun handleProfessionalUpdate(e:UpdateProfessionalException)=
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,e.message!!)

}
class ProfessionalNotFoundException(message: String) : Throwable(message)
class BadProfessionalRequestException(message: String) : Throwable(message)
class UpdateProfessionalException(message: String) : Throwable(message)