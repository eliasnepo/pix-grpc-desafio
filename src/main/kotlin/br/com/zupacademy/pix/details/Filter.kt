package br.com.zupacademy.pix.details

import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.shared.exceptions.PermissionDeniedException
import br.com.zupacademy.shared.exceptions.ResourceNotFoundException
import br.com.zupacademy.shared.httpclients.BacenClient
import br.com.zupacademy.shared.validations.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filter {
    abstract fun filtra(repository: KeyRepository, bacenClient: BacenClient): KeyDetailResponse

    @Introspected
    data class ByPixId(
            @field:NotBlank @field:ValidUUID val clientId: String,
            @field:NotBlank @field:ValidUUID val pixId: String,
    ) : Filter() {

        override fun filtra(repository: KeyRepository, bacenClient: BacenClient): KeyDetailResponse {
            val pix = repository.findById(pixId)
            if (pix.isEmpty) {
                throw ResourceNotFoundException("Chave pix não existe no sistema interno.")
            }

            if (bacenClient.findByKey(pix.get().key).status() == HttpStatus.NOT_FOUND) {
                throw ResourceNotFoundException("Chave pix não existe no sistema do Banco Central.")
            }
            return pix
                    .filter {
                        it.belongsTo(clientId)
                    }
                    .map(KeyDetailResponse.Companion::of)
                    .orElseThrow {
                        PermissionDeniedException("Chave pix não pertence a esse cliente.")
                    }
        }
    }

    @Introspected
    data class ByKey(@field:NotBlank @Size(max = 77) val key: String) : Filter() {
        override fun filtra(repository: KeyRepository, bacenClient: BacenClient): KeyDetailResponse {
            return repository.findByKey(key)
                    .map(KeyDetailResponse.Companion::of)
                    .orElseGet {
                        val response = bacenClient.findByKey(key)
                        when (response.status) {
                            HttpStatus.OK -> response.body()?.toModel()
                            else -> throw ResourceNotFoundException("Chave Pix não consta no sistema do banco central.")
                        }
                    }
        }
    }
}
