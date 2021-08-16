package br.com.zupacademy.shared.httpclients.dto

import java.time.LocalDateTime

data class BacenCreateKeyResponse(
        val keyType: KeyTypeBacen,
        val key: String,
        val bankAccount: BankAccountResponse,
        val owner: OwnerResponse,
        val createdAt: LocalDateTime
)

data class BankAccountResponse(
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: AccountTypeBacen
)

data class OwnerResponse(
        val type: PersonTypeBacen,
        val name: String,
        val taxIdNumber: String
)