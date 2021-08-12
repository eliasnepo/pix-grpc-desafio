package br.com.zupacademy.pix.create.controller

import br.com.zupacademy.KeyRequest
import br.com.zupacademy.KeyResponse
import br.com.zupacademy.PixKeyServiceGrpc
import br.com.zupacademy.pix.create.exceptions.ExistsKeyException
import br.com.zupacademy.pix.create.exceptions.ResourceNotFoundException
import br.com.zupacademy.pix.create.validations.ErrorHandler
import br.com.zupacademy.pix.create.httpclients.ItauClient
import br.com.zupacademy.pix.create.httpclients.dto.AccountsOfClientResponse
import br.com.zupacademy.pix.create.model.Account
import br.com.zupacademy.pix.create.model.Key
import br.com.zupacademy.pix.create.repository.KeyRepository
import br.com.zupacademy.pix.create.toModel
import br.com.zupacademy.pix.create.validate
import io.grpc.stub.StreamObserver
import io.micronaut.context.annotation.Type
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid

@ErrorHandler
@Singleton
class CreateKeyEndpoint(
    val keyRepository: KeyRepository,
    val itauHttpClient: ItauClient
) : PixKeyServiceGrpc.PixKeyServiceImplBase() {

    override fun generateKey(request: KeyRequest, responseObserver: StreamObserver<KeyResponse>) {
        request.validate()

        if (keyRepository.existsByKey(request.key)) {
            throw ExistsKeyException("Chave já existente.")
        }

        var itauClientResponse: HttpResponse<AccountsOfClientResponse>
        try {
            itauClientResponse = itauHttpClient.findByClientId(request.clientId, request.accountType)
        } catch (e: HttpClientResponseException) {
            throw ResourceNotFoundException("O cliente não está na base do Itau.")
        }

        val account = itauClientResponse.body().toModel()
        val key = request.toModel(account)
        keyRepository.save(key)

        val keyResponse = KeyResponse.newBuilder()
            .setPixId(key.id)
            .build()

        responseObserver.onNext(keyResponse)
        responseObserver.onCompleted()
    }
}