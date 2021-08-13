package br.com.zupacademy.pix.create

import br.com.zupacademy.AccountType
import br.com.zupacademy.KeyRequest
import br.com.zupacademy.KeyType
import br.com.zupacademy.PixKeyServiceGrpc
import br.com.zupacademy.pix.create.exceptions.StatusWithDetails
import br.com.zupacademy.pix.create.factory.createValidKey
import br.com.zupacademy.pix.create.factory.itauResponse
import br.com.zupacademy.pix.create.httpclients.ItauClient
import br.com.zupacademy.pix.create.model.Account
import br.com.zupacademy.pix.create.model.Key
import br.com.zupacademy.pix.create.repository.KeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CreateKeyEndpointTestIT(
    val keyRepository: KeyRepository,
    val grpcClient: PixKeyServiceGrpc.PixKeyServiceBlockingStub,
    val itauClient: ItauClient
) {

    private val invalidClientId = UUID.randomUUID().toString()
    private val validClientId = "c56dfef4-7901-44fb-84e2-a2cefb157890"
    private val validAccountType = AccountType.CONTA_CORRENTE
    private val validEmailKey = "elias@zup.com.br"
    private val existingEmailKey = "rafa@zup.com.br"

    @BeforeEach
    fun setUp() {
        keyRepository.deleteAll()
        Mockito.`when`(itauClient.findByClientId(validClientId, validAccountType)).thenReturn(HttpResponse.ok(itauResponse()))
        Mockito.`when`(itauClient.findByClientId(invalidClientId, validAccountType)).thenThrow(HttpClientResponseException::class.java)
    }

    @Test
    fun `should add pix key when valid data`() {
        val request = KeyRequest.newBuilder()
            .setClientId(validClientId)
            .setKeyType(KeyType.EMAIL)
            .setKey(validEmailKey)
            .setAccountType(validAccountType)
            .build()

        val response = grpcClient.generateKey(request)

        with(response) {
            assertNotNull(response.pixId)
            assertTrue(keyRepository.existsByKey(request.key))
        }
    }

    @Test
    fun `should not add pix key when key exists`() {
        val key = createValidKey()
        keyRepository.save(key)

        val request = KeyRequest.newBuilder()
            .setClientId(validClientId)
            .setKeyType(KeyType.EMAIL)
            .setKey(existingEmailKey)
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with (error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave já existente.", status.description)
        }
    }

    @Test
    fun `should not add pix key when client id its not in legacy itau system`() {
        val request = KeyRequest.newBuilder()
            .setClientId(invalidClientId)
            .setKeyType(KeyType.EMAIL)
            .setKey(validEmailKey)
            .setAccountType(validAccountType)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("O cliente não está na base do Itau.", status.description)
        }
    }

    @Test
    fun `should not add pix key when key type is random and the field key isn't empty`() {
        val request = KeyRequest.newBuilder()
                .setAccountType(validAccountType)
                .setKeyType(KeyType.RANDOM)
                .setKey(validEmailKey)
                .setClientId(validClientId)
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `should not add pix key when key type is unknown`() {
        val request = KeyRequest.newBuilder()
                .setKeyType(KeyType.UNKNOWN_KEY)
                .setClientId(validClientId)
                .setAccountType(validAccountType)
                .setKey(validEmailKey)
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `should not add pix key when account type is unknown`() {
        val request = KeyRequest.newBuilder()
                .setKeyType(KeyType.EMAIL)
                .setClientId(validClientId)
                .setAccountType(AccountType.UNKNOWN_ACCOUNT)
                .setKey(validEmailKey)
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `should not add pix key when email key isn't well formed`() {
        val request = KeyRequest.newBuilder()
                .setKeyType(KeyType.EMAIL)
                .setClientId(validClientId)
                .setAccountType(validAccountType)
                .setKey("elias@elias")
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Essa chave não tem formato válido.", status.description)
        }
    }

    @Test
    fun `should not add pix key when cpf key isn't well formed`() {
        val request = KeyRequest.newBuilder()
                .setKeyType(KeyType.CPF)
                .setClientId(validClientId)
                .setAccountType(validAccountType)
                .setKey("1234567890")
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Essa chave não tem formato válido.", status.description)
        }
    }

    @Test
    fun `should not add pix key when phone number key isn't well formed`() {
        val request = KeyRequest.newBuilder()
                .setKeyType(KeyType.PHONE)
                .setClientId(validClientId)
                .setAccountType(validAccountType)
                .setKey("62999999999")
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Essa chave não tem formato válido.", status.description)
        }
    }

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }
}

@Factory
class Clients {
    @Singleton
    fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
            PixKeyServiceGrpc.PixKeyServiceBlockingStub {
        return PixKeyServiceGrpc.newBlockingStub(channel)
    }
}