package br.com.zupacademy.shared.exceptions.handlers

import br.com.zupacademy.shared.exceptions.StatusWithDetails
import br.com.zupacademy.shared.validations.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConstraintViolationhandler : ExceptionHandler<ConstraintViolationException> {

    override fun handle(e: ConstraintViolationException): StatusWithDetails {
        return StatusWithDetails(Status.INVALID_ARGUMENT
                .withDescription(e.message)
                .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ConstraintViolationException
    }


}