package br.com.zupacademy.pix.create.httpclients

import br.com.zupacademy.pix.create.httpclients.dto.AccountsOfClientResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.url}")
interface ItauClient {

    @Get("/clientes/{clienteId}/contas")
    fun findByClientId(@PathVariable clienteId: String, @QueryValue tipo: br.com.zupacademy.AccountType): HttpResponse<AccountsOfClientResponse>
}