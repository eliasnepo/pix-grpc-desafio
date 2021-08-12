package br.com.zupacademy.pix.create.exceptions.handlers

import br.com.zupacademy.pix.create.exceptions.ExistsKeyException
import br.com.zupacademy.pix.create.exceptions.StatusWithDetails
import br.com.zupacademy.pix.create.validations.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ExistsKeyExceptionHandler : ExceptionHandler<ExistsKeyException> {

    override fun handle(e: ExistsKeyException): StatusWithDetails {
        return StatusWithDetails(Status.ALREADY_EXISTS
            .withDescription(e.message)
            .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ExistsKeyException
    }
}