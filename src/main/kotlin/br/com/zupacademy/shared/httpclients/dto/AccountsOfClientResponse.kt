package br.com.zupacademy.shared.httpclients.dto

import br.com.zupacademy.pix.AccountType
import br.com.zupacademy.pix.Account

data class AccountsOfClientResponse (
        val tipo: AccountType,
        val instituicao: InstituicaoResponse,
        val agencia: String,
        val numero: String,
        val titular: TitularResponse
        ) {

    fun toModel(): Account {
        return Account(
            agency = agencia,
            number = numero,
            accountType = tipo,
            ownerName = titular.nome,
            ownerId = titular.id,
            ownerCpf = titular.cpf,
            participant = instituicao.ispb
        )
    }
}

data class InstituicaoResponse (val nome: String, val ispb: String)

data class TitularResponse (val id: String, val nome: String, val cpf: String)