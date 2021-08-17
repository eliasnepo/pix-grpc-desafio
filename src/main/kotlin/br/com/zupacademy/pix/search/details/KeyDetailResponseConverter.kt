package br.com.zupacademy.pix.search.details

import br.com.zupacademy.AccountType
import br.com.zupacademy.KeyDetailResponse
import br.com.zupacademy.KeyType
import com.google.protobuf.Timestamp
import java.time.ZoneOffset

class KeyDetailResponseConverter {
    fun convert(keyInfo: br.com.zupacademy.pix.search.details.KeyDetailResponse): KeyDetailResponse {
        return KeyDetailResponse.newBuilder()
                .setKeyType(KeyType.valueOf(keyInfo.keyType.name))
                .setKey(keyInfo.key)
                .setAccount(KeyDetailResponse.Account.newBuilder()
                        .setAccountType(AccountType.valueOf(keyInfo.accountType.name))
                        .setBranch(keyInfo.account.agency)
                        .setNumber(keyInfo.account.number)
                        .setOwnerName(keyInfo.account.ownerName)
                        .setOwnerCpf(keyInfo.account.ownerCpf)
                        .setParticipant(keyInfo.account.participant)
                        .build())
                .setCreatedAt(keyInfo.createdAt.let {
                    Timestamp.newBuilder()
                            .setSeconds(it.toInstant(ZoneOffset.UTC).epochSecond)
                            .setNanos(it.toInstant(ZoneOffset.UTC).nano)
                            .build()
                })
                .setClientId(keyInfo.clientId)
                .setPixId(keyInfo.pixId)
                .build()
    }
}
