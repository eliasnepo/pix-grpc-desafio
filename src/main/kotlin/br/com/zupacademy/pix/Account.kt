package br.com.zupacademy.pix

import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EnumType
import javax.persistence.Enumerated

@Embeddable
class Account(
        @Column(nullable = false)
        val agency: String,

        @Column(nullable = false)
        val number: String,

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        val accountType: AccountType,

        @Column(nullable = false)
        val ownerName: String
) {
}