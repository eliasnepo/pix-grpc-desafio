package br.com.zupacademy.shared.httpclients.dto

import br.com.zupacademy.pix.Account
import br.com.zupacademy.pix.KeyType
import br.com.zupacademy.pix.search.details.KeyDetailResponse
import java.time.LocalDateTime

data class BacenDetailKeyResponse(
        val keyType: KeyTypeBacen,
        val key: String,
        val bankAccount: BankAccountResponse,
        val owner: OwnerResponse,
        val createdAt: LocalDateTime
) {
    fun toModel(): KeyDetailResponse? {
        return KeyDetailResponse(
            pixId = "",
            clientId = "",
            keyType = KeyType.valueOf(keyType.name),
            key = key,
            accountType = bankAccount.accountType.converter(),
            account = Account(
                    agency = bankAccount.branch,
                    number = bankAccount.accountNumber,
                    accountType = bankAccount.accountType.converter(),
                    ownerId = owner.taxIdNumber,
                    ownerName = owner.name,
                    ownerCpf = owner.taxIdNumber,
                    participant = bankAccount.participant
            ),
            createdAt = createdAt

        )
    }
}
