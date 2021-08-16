package br.com.zupacademy.shared.httpclients.dto

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
    CACC, SVGS;
}

enum class PersonTypeBacen {
    NATURAL_PERSON, LEGAL_PERSON;
}