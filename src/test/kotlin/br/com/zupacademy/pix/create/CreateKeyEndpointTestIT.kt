package br.com.zupacademy.pix.create

import br.com.zupacademy.AccountType
import br.com.zupacademy.KeyRequest
import br.com.zupacademy.KeyType
import br.com.zupacademy.PixKeyServiceGrpc
import br.com.zupacademy.shared.httpclients.ItauClient
import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.pix.factory.*
import br.com.zupacademy.shared.httpclients.BacenClient
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CreateKeyEndpointTestIT(
        val keyRepository: KeyRepository,
        val grpcClient: PixKeyServiceGrpc.PixKeyServiceBlockingStub,
        val itauClient: ItauClient,
        val bacenClient: BacenClient
) {

    private val invalidClientId = UUID.randomUUID().toString()
    private val validClientId = "96be5bb9-6abd-4543-9876-a0605c26606a"
    private val validAccountType = AccountType.CONTA_CORRENTE
    private val validCpfKey = "12345678911"
    private val existingKeyInBcb = "99988877711"

    @BeforeEach
    fun setUp() {
        keyRepository.deleteAll()
    }

    @Test
    fun `should add pix key when valid data`() {
        Mockito.`when`(itauClient.findByClientId(validClientId, validAccountType)).thenReturn(HttpResponse.ok(itauResponse()))
        Mockito.`when`(bacenClient.registerKey(createBacenCreateKeyRequest(validCpfKey))).thenReturn(HttpResponse.created(createBacenCreateKeyResponse()))

        val request = KeyRequest.newBuilder()
            .setClientId(validClientId)
            .setKeyType(KeyType.CPF)
            .setKey(validCpfKey)
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
        val pix = createValidKey() // Pix Key Type: CPF. Pix Account Type: CONTA_CORRENTE
        keyRepository.save(pix)

        val request = KeyRequest.newBuilder()
            .setClientId(pix.account.ownerId)
            .setKeyType(KeyType.CPF)
            .setKey(pix.key)
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
            .setKeyType(KeyType.CPF)
            .setKey(validCpfKey)
            .setAccountType(validAccountType)
            .build()

        Mockito.`when`(itauClient.findByClientId(invalidClientId, validAccountType)).thenThrow(HttpClientResponseException::class.java)

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
                .setKey(validCpfKey)
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
                .setKey(validCpfKey)
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
                .setKey(validCpfKey)
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

    @Test
    fun `should not add pix key when key exists in bcb system`() {
        Mockito.`when`(itauClient.findByClientId(validClientId, validAccountType)).thenReturn(HttpResponse.ok(itauResponse()))
        val http = HttpResponse.status<Any>(HttpStatus.UNPROCESSABLE_ENTITY)
        BDDMockito.`when`(bacenClient.registerKey(createBacenCreateKeyRequest(existingKeyInBcb))).thenThrow(HttpClientResponseException("Mensagem", http))

        val request = KeyRequest.newBuilder()
                .setAccountType(AccountType.CONTA_CORRENTE)
                .setKeyType(KeyType.CPF)
                .setKey(existingKeyInBcb)
                .setClientId("96be5bb9-6abd-4543-9876-a0605c26606a")
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with(error) {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("Chave pix já registrada.", status.description)
        }
    }

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BacenClient::class)
    fun bacenClient(): BacenClient {
        return Mockito.mock(BacenClient::class.java)
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