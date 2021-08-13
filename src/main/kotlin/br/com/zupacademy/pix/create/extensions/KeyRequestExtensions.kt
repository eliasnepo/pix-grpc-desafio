package br.com.zupacademy.pix.create

import br.com.zupacademy.AccountType
import br.com.zupacademy.KeyRequest
import br.com.zupacademy.pix.create.model.Account
import br.com.zupacademy.pix.create.model.Key
import br.com.zupacademy.pix.create.model.enums.KeyType
import java.util.*

fun KeyRequest.validate() {
    if (keyType.ordinal != br.com.zupacademy.KeyType.RANDOM_VALUE && key.isNullOrBlank()) {
        throw IllegalArgumentException("A chave não pode ser vazia ou nula")
    }
    if (key.length > 77) {
        throw IllegalArgumentException("A chave não pode ter mais que 77 caracteres")
    }
    if (clientId.isNullOrBlank()) {
        throw IllegalArgumentException("O Id do cliente não pode ser vazio ou nulo")
    }
    if (keyType == null || keyType == br.com.zupacademy.KeyType.UNKNOWN_KEY) {
        throw IllegalArgumentException("O tipo da chave não pode ser nulo ou desconhecido")
    }
    if (accountType == null || accountType == AccountType.UNKNOWN_ACCOUNT) {
        throw IllegalArgumentException("O tipo da conta não pode ser nulo ou desconhecido")
    }

    val keyTypeModel = KeyType.valueOf(keyType.name)
    if (keyTypeModel.validate(key) == false) {
        throw IllegalArgumentException("Essa chave não tem formato válido.")
    }
}

fun KeyRequest.toModel(account: Account) : Key {
    return Key(
        key = if (keyType.ordinal == 4) UUID.randomUUID().toString() else key,
        keyType = KeyType.valueOf(keyType.name),
        account = account
    )
}