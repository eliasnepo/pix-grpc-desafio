package br.com.zupacademy.pix.delete

import br.com.zupacademy.shared.validations.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class DeleteKeyDTO (
        @field:NotBlank
        @field:ValidUUID(message = "Não é um formato de UUID válido")
        val pixId: String,

        @field:NotBlank
        @field:ValidUUID(message = "Não é um formato de UUID válido")
        val clientId: String
        )