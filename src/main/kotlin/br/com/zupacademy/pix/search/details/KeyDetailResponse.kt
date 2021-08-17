package br.com.zupacademy.pix.search.details

import br.com.zupacademy.pix.Account
import br.com.zupacademy.pix.AccountType
import br.com.zupacademy.pix.Key
import br.com.zupacademy.pix.KeyType
import java.time.LocalDateTime

class KeyDetailResponse(
        val pixId: String? = "",
        val clientId: String? = "",
        val keyType: KeyType,
        val key: String,
        val accountType: AccountType,
        val account: Account,
        val createdAt: LocalDateTime
) {

    companion object {
        fun of(pix: Key): KeyDetailResponse {
            return KeyDetailResponse(
                    pixId = pix.id,
                    clientId = pix.account.ownerId,
                    keyType = pix.keyType,
                    key = pix.key,
                    accountType = pix.account.accountType,
                    account = pix.account,
                    createdAt = pix.createdAt
            )
        }
    }
}