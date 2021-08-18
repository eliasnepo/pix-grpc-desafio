package br.com.zupacademy.pix.details

import br.com.zupacademy.DetailsKeyServiceGrpc
import br.com.zupacademy.KeyDetailRequest
import br.com.zupacademy.KeyDetailResponse
import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.shared.httpclients.BacenClient
import br.com.zupacademy.shared.validations.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandler
@Singleton
class DetailsOfKeyEndpoint(
        val keyRepository: KeyRepository,
        val bacenClient: BacenClient,
        val validator: Validator
) : DetailsKeyServiceGrpc.DetailsKeyServiceImplBase() {

    override fun keyDetail(request: KeyDetailRequest, responseObserver: StreamObserver<KeyDetailResponse>) {

        val filter = request.toModel(validator)
        val keyInfo = filter.filtra(repository = keyRepository, bacenClient = bacenClient)

        responseObserver.onNext(KeyDetailResponseConverter().convert(keyInfo))
        responseObserver.onCompleted()
    }
}