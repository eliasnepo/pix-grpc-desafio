package br.com.zupacademy.pix.create

import br.com.zupacademy.KeyRequest
import br.com.zupacademy.shared.exceptions.ExistsKeyException
import br.com.zupacademy.shared.exceptions.ResourceNotFoundException
import br.com.zupacademy.shared.httpclients.ItauClient
import br.com.zupacademy.pix.Key
import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.shared.exceptions.PermissionDeniedException
import br.com.zupacademy.shared.httpclients.BacenClient
import br.com.zupacademy.shared.httpclients.dto.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class CreateKeyService(
        val keyRepository: KeyRepository,
        val itauHttpClient: ItauClient,
        val bacenHttpClient: BacenClient
) {

    fun register(request: KeyRequest): Key {
        if (keyRepository.existsByKey(request.key)) {
            throw ExistsKeyException("Chave já existente.")
        }

        val itauClientResponse: HttpResponse<AccountsOfClientResponse>
        try {
            itauClientResponse = itauHttpClient.findByClientId(request.clientId, request.accountType)
        } catch (e: HttpClientResponseException) {
            throw ResourceNotFoundException("O cliente não está na base do Itau.")
        }

        if (itauClientResponse.status() == HttpStatus.NOT_FOUND) {
            throw ResourceNotFoundException("O cliente não está na base do Itau.")
        }
        val bankAccount = itauClientResponse.body()!!

        var keyBacen = ""
        try {
            val responseBacen = bacenHttpClient.registerKey(BacenCreateKeyRequest(
                    keyType = KeyTypeBacen.valueOf(request.keyType.name),
                    key = request.key,
                    bankAccount = BankAccountRequest(
                            description = "Optional",
                            branch = bankAccount.agencia,
                            accountNumber = bankAccount.numero,
                            participant = bankAccount.instituicao.ispb,
                            accountType = request.accountType.convert()
                    ),
                    owner = OwnerRequest(
                            name = bankAccount.titular.nome,
                            taxIdNumber = bankAccount.titular.cpf,
                            type = PersonTypeBacen.LEGAL_PERSON
                    )
            ))
            keyBacen = responseBacen.body()!!.key
        } catch (e: HttpClientResponseException) {
            if (e.status == HttpStatus.UNPROCESSABLE_ENTITY) {
                throw PermissionDeniedException("Chave pix já registrada.")
            }
        }

        val account = itauClientResponse.body()!!.toModel()
        val key = request.toModel(account, keyBacen)
        return keyRepository.save(key)
    }
}