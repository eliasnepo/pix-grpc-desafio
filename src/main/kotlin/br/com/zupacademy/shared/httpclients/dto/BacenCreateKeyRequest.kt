package br.com.zupacademy.shared.httpclients.dto

import br.com.zupacademy.pix.AccountType


data class BacenCreateKeyRequest(
        val keyType: KeyTypeBacen,
        val key: String?,
        val bankAccount: BankAccountRequest,
        val owner: OwnerRequest
)

data class BankAccountRequest(
        val description: String?,
        val participant: String,
        val branch: String,
        val accountNumber: String,
        val accountType: AccountTypeBacen
)

data class OwnerRequest(
        val type: PersonTypeBacen,
        val name: String,
        val taxIdNumber: String
)

enum class KeyTypeBacen {
    CPF, CNPJ, PHONE, EMAIL, RANDOM;
}

enum class AccountTypeBacen {
    CACC {
        override fun converter(): AccountType {
            return AccountType.CONTA_CORRENTE
        }
    }, SVGS {
        override fun converter(): AccountType {
            return AccountType.CONTA_POUPANCA
        }
    };

    abstract fun converter(): AccountType
}

enum class PersonTypeBacen {
    NATURAL_PERSON, LEGAL_PERSON;
}