package br.com.zupacademy.pix.delete

import br.com.zupacademy.DeleteKeyRequest
import br.com.zupacademy.DeleteKeyServiceGrpc
import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.pix.factory.createValidKey
import br.com.zupacademy.pix.factory.itauResponse
import br.com.zupacademy.shared.httpclients.BacenClient
import br.com.zupacademy.shared.httpclients.ItauClient
import br.com.zupacademy.shared.httpclients.dto.BacenDeleteKeyRequest
import br.com.zupacademy.shared.httpclients.dto.BacenDeleteKeyResponse
import br.com.zupacademy.shared.httpclients.dto.ClientInfosResponse
import br.com.zupacademy.shared.httpclients.dto.InstituicaoResponse
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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeleteKeyTestIT(
        val keyRepository: KeyRepository,
        val grpcClient: DeleteKeyServiceGrpc.DeleteKeyServiceBlockingStub,
        val itauClient: ItauClient,
        val bacenClient: BacenClient
) {

    private val validClientId = "96be5bb9-6abd-4543-9876-a0605c26606a"
    private val invalidClientId = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        keyRepository.deleteAll()
    }

    @Test
    fun `should remove key when valid data`() {
        val pix = createValidKey()
        keyRepository.save(pix)

        Mockito.`when`(itauClient.findClientInfos(pix.account.ownerId)).thenReturn(HttpResponse.ok(itauAccountResponse()))
        Mockito.`when`(bacenClient.deleteKey(pix.key, deleteKeyRequest())).thenReturn(HttpResponse.ok(deleteKeyResponse()))

        val request = DeleteKeyRequest.newBuilder()
                .setClientId(pix.account.ownerId)
                .setPixId(pix.id)
                .build()

        grpcClient.deleteKey(request)

        assertFalse(keyRepository.existsByKey(pix.key))
        assertFalse(keyRepository.existsById(pix.id))
    }

    @Test
    fun `should throw exception when pixId is empty`() {
        val request = DeleteKeyRequest.newBuilder()
                .setPixId("")
                .setClientId(UUID.randomUUID().toString())
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleteKey(request)
        }
        with (error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
            assertTrue(status.description!!.contains("must not be blank"))
            assertTrue(status.description!!.contains("valid UUID"))
        }
    }

    @Test
    fun `should throw exception when clientId is empty`() {
        val request = DeleteKeyRequest.newBuilder()
                .setPixId(UUID.randomUUID().toString())
                .setClientId("")
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleteKey(request)
        }
        with (error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
            assertTrue(status.description!!.contains("must not be blank"))
            assertTrue(status.description!!.contains("valid UUID"))
        }
    }

    @Test
    fun `should throw exception when pixId does not exists in database`() {
        val request = DeleteKeyRequest.newBuilder()
                .setPixId(UUID.randomUUID().toString())
                .setClientId(validClientId)
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleteKey(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertNotNull(status.description)
            assertTrue(status.description!!.contains("Pix id não existente."))
        }
    }

    @Test
    fun `should throw exception when clientId is not in the erp system`() {
        val key = createValidKey()
        keyRepository.save(key)

        Mockito.`when`(itauClient.findClientInfos(invalidClientId)).thenReturn(HttpResponse.notFound())

        val request = DeleteKeyRequest.newBuilder()
                .setPixId(key.id)
                .setClientId(invalidClientId)
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleteKey(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertNotNull(status.description)
            assertTrue(status.description!!.toLowerCase().contains("cliente não existente".toLowerCase()))
        }
    }

    @Test
    fun `should not delete key when pixId is null`() {
        val request = DeleteKeyRequest.newBuilder()
                .setClientId(validClientId)
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleteKey(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
            assertTrue(status.description!!.contains("must not be blank"))
        }
    }

    @Test
    fun `should not delete key when clientId is null`() {
        val request = DeleteKeyRequest.newBuilder()
                .setPixId(UUID.randomUUID().toString())
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleteKey(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertNotNull(status.description)
            assertTrue(status.description!!.contains("must not be blank"))
        }
    }

    @Test
    fun `should not delete key when client id is not equivalent to owner id of the key`() {
        // Arrange
        val key = createValidKey()
        keyRepository.save(key)

        val clientId = UUID.randomUUID().toString()
        Mockito.`when`(itauClient.findClientInfos(clientId))
                .thenReturn(HttpResponse.ok(ClientInfosResponse(
                        id = clientId,
                        nome = "Elias",
                        cpf = "11122233311",
                        instituicao = InstituicaoResponse(
                                nome = "ITAU",
                                ispb = "60701190"
                        ))))

        val request = DeleteKeyRequest.newBuilder()
                .setPixId(key.id)
                .setClientId(clientId)
                .build()

        // Act
        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleteKey(request)
        }

        // Assert
        with(error) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertNotNull(status.description)
            assertTrue(status.description!!.toLowerCase().contains("não é dono desta chave pix".toLowerCase()))
        }
    }

    @Test
    fun `bacen client should throw exception when ispb is invalid`() {
        val pix = createValidKey()
        keyRepository.save(pix)

        Mockito.`when`(itauClient.findClientInfos(pix.account.ownerId)).thenReturn(HttpResponse.ok(ClientInfosResponse(
                id = "96be5bb9-6abd-4543-9876-a0605c26606a",
                nome = "Elias",
                cpf = "22233344411",
                instituicao = InstituicaoResponse(
                        nome = "ITAU",
                        ispb = "invalid-ispb"
                )
        )))
        val http = HttpResponse.status<Any>(HttpStatus.FORBIDDEN)
        BDDMockito.`when`(bacenClient.deleteKey(pix.key, BacenDeleteKeyRequest(pix.key, "invalid-ispb"))).thenThrow(HttpClientResponseException("Mensagem", http))

        val request = DeleteKeyRequest.newBuilder()
                .setPixId(pix.id)
                .setClientId(pix.account.ownerId)
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleteKey(request)
        }

        with(error) {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertNotNull(status.description)
            assertEquals("Proibido realizar operação.", status.description)
        }
    }

    @Test
    fun `bacen client should return status not found when key does not exists`() {
        val pix = createValidKey()
        keyRepository.save(pix)
        val infoAccount = itauAccountResponse()
        Mockito.`when`(itauClient.findClientInfos(pix.account.ownerId)).thenReturn(HttpResponse.ok(itauAccountResponse()))
        Mockito.`when`(bacenClient.deleteKey(pix.key, BacenDeleteKeyRequest(pix.key, infoAccount.instituicao.ispb))).thenReturn(HttpResponse.notFound())

        val request = DeleteKeyRequest.newBuilder()
                .setPixId(pix.id)
                .setClientId(pix.account.ownerId)
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.deleteKey(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertNotNull(status.description)
            assertEquals("Chave pix não encontrada.", status.description)
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
            DeleteKeyServiceGrpc.DeleteKeyServiceBlockingStub {
        return DeleteKeyServiceGrpc.newBlockingStub(channel)
    }
}