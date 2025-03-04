package br.com.zupacademy.shared.validations

import br.com.zupacademy.shared.exceptions.StatusWithDetails
import io.grpc.BindableService
import io.grpc.stub.StreamObserver
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerInterceptor(@Inject private val resolver: ExceptionHandlerResolver)
    : MethodInterceptor<BindableService, Any> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun intercept(context: MethodInvocationContext<BindableService, Any?>): Any? {

        try {
            return context.proceed()
        } catch (e: Exception) {
            logger.error("Handling with exception: ${e.javaClass.name}. Target Method: ${context.targetMethod}", e)

            val handler = resolver.resolve(e)
            val status: StatusWithDetails = handler.handle(e)

            GrpcEndpointArguments(context).response().onError(status.asRuntimeException())
            return null
        }
    }

    private class GrpcEndpointArguments(val context : MethodInvocationContext<BindableService, Any?>) {

        fun response(): StreamObserver<*> {
            return context.parameterValues[1] as StreamObserver<*>
        }
    }
}