package br.com.zupacademy.shared.httpclients

import br.com.zupacademy.shared.httpclients.dto.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.url}")
interface BacenClient {

    @Post(value = "/keys", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun registerKey(@Body request: BacenCreateKeyRequest): HttpResponse<BacenCreateKeyResponse>

    @Delete(value = "/keys/{key}", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun deleteKey(@PathVariable key: String, @Body request: BacenDeleteKeyRequest): HttpResponse<BacenDeleteKeyResponse>

    @Get(value = "/keys/{key}", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun findByKey(@PathVariable key: String): HttpResponse<BacenDetailKeyResponse>
}