package br.com.zupacademy.pix.create.validations

import br.com.zupacademy.pix.create.exceptions.ResourceNotFoundException
import br.com.zupacademy.pix.create.exceptions.StatusWithDetails
import io.grpc.Status
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(@Inject val handlers: List<ExceptionHandler<Exception>>) {

    private var defaultHandler: ExceptionHandler<Exception> = DefaultExceptionHandler()

    constructor(handlers: List<ExceptionHandler<Exception>>, defaultHandler: ExceptionHandler<Exception>)
            : this(handlers) {
        this.defaultHandler = defaultHandler
    }

    fun resolve(e: Exception): ExceptionHandler<Exception> {
        val foundHandlers = handlers.filter { h -> h.supports(e) }
        if(foundHandlers.size > 1) {
            throw IllegalStateException(
                "More than one handler to exception ${e.javaClass.name}: $foundHandlers")
        }

        return foundHandlers.firstOrNull() ?: defaultHandler
    }
}

class DefaultExceptionHandler : ExceptionHandler<Exception> {
    override fun handle(e: Exception): StatusWithDetails {
        val status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            is ResourceNotFoundException -> Status.NOT_FOUND.withDescription(e.message)
            else -> Status.UNKNOWN
        }
        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}