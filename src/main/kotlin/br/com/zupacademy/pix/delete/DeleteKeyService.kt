package br.com.zupacademy.pix.delete

import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.shared.exceptions.ResourceNotFoundException
import br.com.zupacademy.shared.httpclients.ItauClient
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class DeleteKeyService (val keyRepository: KeyRepository, val itauClient: ItauClient) {

    fun delete(@Valid request: DeleteKeyDTO) {
        val key = keyRepository.findById(request.pixId).orElseThrow { ResourceNotFoundException("Pix id não existente.") }

        val itauClientResponse = itauClient.findClientInfos(request.clientId)

        if (itauClientResponse.status() == HttpStatus.NOT_FOUND) {
            throw ResourceNotFoundException("Cliente não existente no sistema do Itaú")
        }

        if (key.account.ownerId != itauClientResponse.body()!!.id) {
            throw IllegalStateException("Esse cliente não é dono desta chave pix.")
        }

        keyRepository.delete(key)
    }
}