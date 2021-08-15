package br.com.zupacademy.pix.create

import br.com.zupacademy.KeyRequest
import br.com.zupacademy.shared.exceptions.ExistsKeyException
import br.com.zupacademy.shared.exceptions.ResourceNotFoundException
import br.com.zupacademy.shared.httpclients.ItauClient
import br.com.zupacademy.shared.httpclients.dto.AccountsOfClientResponse
import br.com.zupacademy.pix.Key
import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.pix.create.toModel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

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

        if (itauClientResponse.status() == HttpStatus.NOT_FOUND) {
            throw ResourceNotFoundException("O cliente não está na base do Itau.")
        }

        val account = itauClientResponse.body()!!.toModel()
        val key = request.toModel(account)
        return keyRepository.save(key)
    }
}