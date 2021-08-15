package br.com.zupacademy.pix.delete

import br.com.zupacademy.shared.httpclients.dto.ClientInfosResponse
import br.com.zupacademy.shared.httpclients.dto.InstituicaoResponse
import io.micronaut.context.annotation.Factory
import io.micronaut.http.HttpResponse

@Factory
fun itauAccountResponse() : ClientInfosResponse {
    return ClientInfosResponse(
            id = "96be5bb9-6abd-4543-9876-a0605c26606a",
            nome = "Elias",
            cpf = "22233344411",
            instituicao = InstituicaoResponse(
                    nome = "ITAU",
                    ispb = "0001"
            )
    )
}