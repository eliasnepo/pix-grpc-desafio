package br.com.zupacademy.pix.create.factory

import br.com.zupacademy.shared.httpclients.dto.AccountsOfClientResponse
import br.com.zupacademy.shared.httpclients.dto.InstituicaoResponse
import br.com.zupacademy.shared.httpclients.dto.TitularResponse
import br.com.zupacademy.pix.Account
import br.com.zupacademy.pix.Key
import br.com.zupacademy.pix.AccountType
import br.com.zupacademy.pix.KeyType
import io.micronaut.context.annotation.Factory

@Factory
fun itauResponse(): AccountsOfClientResponse {
    return AccountsOfClientResponse(
            tipo = AccountType.CONTA_CORRENTE,
            titular = TitularResponse(
                    id = "c56dfef4-7901-44fb-84e2-a2cefb157890",
                    nome = "Elias",
                    cpf = "12345678911"
            ),
            agencia = "0001",
            numero = "1234",
            instituicao = InstituicaoResponse(
                    nome = "ITAU",
                    ispb = "123"
            ),
    )
}

@Factory
fun createValidKey(): Key {
    return Key(key = "rafa@zup.com.br", keyType = KeyType.EMAIL,
            Account("0001", "1234", accountType = AccountType.CONTA_CORRENTE,
                    ownerName = "Rafael"))
}