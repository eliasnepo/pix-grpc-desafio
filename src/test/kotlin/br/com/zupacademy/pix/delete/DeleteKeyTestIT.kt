package br.com.zupacademy.pix.delete

import br.com.zupacademy.DeleteKeyRequest
import br.com.zupacademy.DeleteKeyServiceGrpc
import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.pix.create.factory.createValidKey
import br.com.zupacademy.shared.httpclients.ItauClient
import br.com.zupacademy.shared.httpclients.dto.ClientInfosResponse
import br.com.zupacademy.shared.httpclients.dto.InstituicaoResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class DeleteKeyTestIT(
        val keyRepository: KeyRepository,
        val grpcClient: DeleteKeyServiceGrpc.DeleteKeyServiceBlockingStub,
        val itauClient: ItauClient
) {

    private val validClientId = "96be5bb9-6abd-4543-9876-a0605c26606a"
    private val invalidClientId = UUID.randomUUID().toString()

    @BeforeEach
    fun setUp() {
        keyRepository.deleteAll()
        Mockito.`when`(itauClient.findClientInfos(validClientId)).thenReturn(HttpResponse.ok(itauAccountResponse()))
        Mockito.`when`(itauClient.findClientInfos(invalidClientId)).thenReturn(HttpResponse.notFound())
    }

    @Test
    fun `should remove key when valid data`() {
        val pix = createValidKey()
        keyRepository.save(pix)

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
                                ispb = "1234"
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

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient {
        return Mockito.mock(ItauClient::class.java)
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