package br.com.zupacademy.pix.delete

import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.shared.exceptions.ResourceNotFoundException
import br.com.zupacademy.shared.httpclients.BacenClient
import br.com.zupacademy.shared.httpclients.ItauClient
import br.com.zupacademy.shared.httpclients.dto.BacenDeleteKeyRequest
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class DeleteKeyService(
        val keyRepository: KeyRepository,
        val itauClient: ItauClient,
        val bacenClient: BacenClient
) {

    fun delete(@Valid request: DeleteKeyDTO) {
        val pix = keyRepository.findById(request.pixId).orElseThrow { ResourceNotFoundException("Pix id não existente.") }

        val itauClientResponse = itauClient.findClientInfos(request.clientId)

        if (itauClientResponse.status() == HttpStatus.NOT_FOUND) {
            throw ResourceNotFoundException("Cliente não existente no sistema do Itaú")
        }

        if (pix.account.ownerId != itauClientResponse.body()!!.id) {
            throw IllegalStateException("Esse cliente não é dono desta chave pix.")
        }

        val bacenClientResponse = bacenClient.deleteKey(pix.key, BacenDeleteKeyRequest(
                key = pix.key,
                participant = itauClientResponse.body()!!.instituicao.ispb
        ))

        when (bacenClientResponse.status) {
            HttpStatus.FORBIDDEN -> throw IllegalStateException("Proibido realizar operação.")
            HttpStatus.NOT_FOUND -> throw ResourceNotFoundException("Chave pix não encontrada.")
        }

        keyRepository.delete(pix)
    }
}