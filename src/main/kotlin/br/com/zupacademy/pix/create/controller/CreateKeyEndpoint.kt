package br.com.zupacademy.pix.create.controller

import br.com.zupacademy.KeyRequest
import br.com.zupacademy.KeyResponse
import br.com.zupacademy.PixKeyServiceGrpc
import br.com.zupacademy.pix.create.validations.ErrorHandler
import br.com.zupacademy.pix.create.validate
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class CreateKeyEndpoint(
    val service: CreateKeyService
) : PixKeyServiceGrpc.PixKeyServiceImplBase() {

    override fun generateKey(request: KeyRequest, responseObserver: StreamObserver<KeyResponse>) {
        request.validate()
        val key = service.register(request)

        val keyResponse = KeyResponse.newBuilder()
            .setPixId(key.id)
            .build()

        responseObserver.onNext(keyResponse)
        responseObserver.onCompleted()
    }
}