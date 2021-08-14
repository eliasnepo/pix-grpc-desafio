package br.com.zupacademy.shared.httpclients

import br.com.zupacademy.shared.httpclients.dto.AccountsOfClientResponse
import br.com.zupacademy.shared.httpclients.dto.ClientInfosResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.url}")
interface ItauClient {

    @Get("/clientes/{clienteId}/contas")
    fun findByClientId(@PathVariable clienteId: String, @QueryValue tipo: br.com.zupacademy.AccountType): HttpResponse<AccountsOfClientResponse>

    @Get("/clientes/{clienteId}")
    fun findClientInfos(@PathVariable clienteId: String): HttpResponse<ClientInfosResponse>
}