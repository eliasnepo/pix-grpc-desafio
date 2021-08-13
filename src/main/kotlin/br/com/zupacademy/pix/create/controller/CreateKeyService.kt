package br.com.zupacademy.pix.create.controller

import br.com.zupacademy.KeyRequest
import br.com.zupacademy.pix.create.exceptions.ExistsKeyException
import br.com.zupacademy.pix.create.exceptions.ResourceNotFoundException
import br.com.zupacademy.pix.create.httpclients.ItauClient
import br.com.zupacademy.pix.create.httpclients.dto.AccountsOfClientResponse
import br.com.zupacademy.pix.create.model.Key
import br.com.zupacademy.pix.create.repository.KeyRepository
import br.com.zupacademy.pix.create.toModel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class CreateKeyService(
        val keyRepository: KeyRepository,
        val itauHttpClient: ItauClient,
) {

    fun register(request: KeyRequest): Key {
        if (keyRepository.existsByKey(request.key)) {
            throw ExistsKeyException("Chave já existente.")
        }

        var itauClientResponse: HttpResponse<AccountsOfClientResponse>
        try {
            itauClientResponse = itauHttpClient.findByClientId(request.clientId, request.accountType)
        } catch (e: HttpClientResponseException) {
            throw ResourceNotFoundException("O cliente não está na base do Itau.")
        }

        val account = itauClientResponse.body()!!.toModel()
        val key = request.toModel(account)
        return keyRepository.save(key)
    }
}