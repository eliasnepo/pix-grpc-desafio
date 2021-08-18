package br.com.zupacademy.pix.factory

import br.com.zupacademy.pix.Account
import br.com.zupacademy.pix.Key
import br.com.zupacademy.pix.AccountType
import br.com.zupacademy.pix.KeyType
import br.com.zupacademy.shared.httpclients.dto.*
import io.micronaut.context.annotation.Factory
import java.time.LocalDateTime

@Factory
fun createValidKey(): Key {
    return Key(key = "12345678911", keyType = KeyType.CPF,
            Account("0001", "1234", accountType = AccountType.CONTA_CORRENTE,
                    ownerName = "Elias", ownerId = "96be5bb9-6abd-4543-9876-a0605c26606a", ownerCpf = "12345678900", participant = "60701190"))
}

@Factory
fun createDynamicValidKey(key: String, clientId: String): Key {
    return Key(key = key, keyType = KeyType.CPF,
            Account("0001", "1234", accountType = AccountType.CONTA_CORRENTE,
                    ownerName = "Elias", ownerId = clientId, ownerCpf = "12345678900", participant = "60701190"))
}

@Factory
fun itauResponse(): AccountsOfClientResponse {
    return AccountsOfClientResponse(
            tipo = AccountType.CONTA_CORRENTE,
            titular = TitularResponse(
                    id = "96be5bb9-6abd-4543-9876-a0605c26606a",
                    nome = "Elias",
                    cpf = "12345678911"
            ),
            agencia = "0001",
            numero = "1234",
            instituicao = InstituicaoResponse(
                    nome = "ITAU",
                    ispb = "60701190"
            ),
    )
}

@Factory
fun createBacenCreateKeyRequest(key: String): BacenCreateKeyRequest {
    val validKey = createValidKey()
    val itauResponse = itauResponse()
    return BacenCreateKeyRequest(
            keyType = KeyTypeBacen.valueOf(validKey.keyType.name),
            key = key,
            bankAccount = BankAccountRequest(
                    description = "Optional",
                    branch = itauResponse.agencia,
                    accountNumber = itauResponse.numero,
                    participant = itauResponse.instituicao.ispb,
                    accountType = AccountTypeBacen.CACC
            ),
            owner = OwnerRequest(
                    name = itauResponse.titular.nome,
                    taxIdNumber = itauResponse.titular.cpf,
                    type = PersonTypeBacen.LEGAL_PERSON
            )
    )
}

@Factory
fun createBacenCreateKeyResponse(): BacenCreateKeyResponse {
    val validKey = createValidKey()
    val bacenCreateRequest = createBacenCreateKeyRequest(validKey.key)
    val itauResponse = itauResponse()
    return BacenCreateKeyResponse(
            keyType = KeyTypeBacen.valueOf(bacenCreateRequest.keyType.name),
            key = validKey.key,
            bankAccount = BankAccountResponse(
                    participant = bacenCreateRequest.bankAccount.participant,
                    branch = bacenCreateRequest.bankAccount.branch,
                    accountNumber = bacenCreateRequest.bankAccount.accountNumber,
                    accountType = AccountTypeBacen.valueOf(bacenCreateRequest.bankAccount.accountType.name),
            ),
            owner = OwnerResponse(
                    type = PersonTypeBacen.LEGAL_PERSON,
                    name = itauResponse.titular.nome,
                    taxIdNumber = itauResponse.titular.cpf
            ),
            createdAt = LocalDateTime.now()
    )
}