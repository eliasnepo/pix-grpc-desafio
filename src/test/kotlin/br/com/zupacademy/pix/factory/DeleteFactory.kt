package br.com.zupacademy.pix.delete

import br.com.zupacademy.pix.factory.createValidKey
import br.com.zupacademy.pix.factory.itauResponse
import br.com.zupacademy.shared.httpclients.dto.BacenDeleteKeyRequest
import br.com.zupacademy.shared.httpclients.dto.BacenDeleteKeyResponse
import br.com.zupacademy.shared.httpclients.dto.ClientInfosResponse
import br.com.zupacademy.shared.httpclients.dto.InstituicaoResponse
import io.micronaut.context.annotation.Factory
import io.micronaut.http.HttpResponse
import java.time.LocalDateTime

@Factory
fun itauAccountResponse() : ClientInfosResponse {
    return ClientInfosResponse(
            id = "96be5bb9-6abd-4543-9876-a0605c26606a",
            nome = "Elias",
            cpf = "22233344411",
            instituicao = InstituicaoResponse(
                    nome = "ITAU",
                    ispb = "60701190"
            )
    )
}

@Factory
fun deleteKeyRequest() : BacenDeleteKeyRequest {
    val pix = createValidKey()
    val itauResponse = itauResponse()
    return BacenDeleteKeyRequest(
            key = pix.key,
            participant = itauResponse.instituicao.ispb
    )
}

@Factory
fun deleteKeyResponse() : BacenDeleteKeyResponse {
    val pix = createValidKey()
    val itauResponse = itauResponse()
    return BacenDeleteKeyResponse(
            key = pix.key,
            participant = itauResponse.instituicao.ispb,
            deletedAt = LocalDateTime.now()
    )
}