package br.com.zupacademy.pix

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class Key(
        @Column(unique = true, nullable = false)
        val key: String,

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        val keyType: KeyType,

        @Embedded
        val account: Account,
) {

    @Id
    @Column(nullable = false)
    val id: String = UUID.randomUUID().toString()

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

    fun belongsTo(clientId: String) : Boolean{
        return this.account.ownerId == clientId
    }
}