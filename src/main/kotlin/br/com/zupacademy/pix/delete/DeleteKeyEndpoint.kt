package br.com.zupacademy.pix.delete

import br.com.zupacademy.DeleteKeyRequest
import br.com.zupacademy.DeleteKeyResponse
import br.com.zupacademy.DeleteKeyServiceGrpc
import br.com.zupacademy.shared.validations.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeleteKeyEndpoint(val service: DeleteKeyService) : DeleteKeyServiceGrpc.DeleteKeyServiceImplBase() {

    override fun deleteKey(request: DeleteKeyRequest, responseObserver: StreamObserver<DeleteKeyResponse>) {
        val requestDto = request.toModel()
        service.delete(requestDto)

        responseObserver.onNext(
                DeleteKeyResponse.newBuilder()
                .setPixId(request.pixId)
                .build()
        )
        responseObserver.onCompleted()
    }
}