package br.com.zupacademy.pix.listAll

import br.com.zupacademy.*
import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.pix.factory.createDynamicValidKey
import br.com.zupacademy.pix.factory.createValidKey
import br.com.zupacademy.shared.httpclients.BacenClient
import br.com.zupacademy.shared.httpclients.ItauClient
import br.com.zupacademy.shared.httpclients.dto.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class AllKeysTestIT(val repository: KeyRepository,
                                val grpcClient: AllKeysServiceGrpc.AllKeysServiceBlockingStub,
                                val itauClient: ItauClient
) {

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `should throw exception when client id is empty`() {
        val request = FindAllKeysRequest.newBuilder()
                .setClientId("")
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.findAll(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O id do cliente deve estar preenchido.", status.description)
        }
    }

    @Test
    fun `should throw exception when client id is null`() {
        val request = FindAllKeysRequest.newBuilder()
                .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.findAll(request)
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O id do cliente deve estar preenchido.", status.description)
        }
    }

    @Test
    fun `should throw exception when client id does not exists in itau system`() {
        val clientId = UUID.randomUUID().toString()
        val request = FindAllKeysRequest.newBuilder()
                .setClientId(clientId)
                .build()

        Mockito.`when`(itauClient.findClientInfos(clientId)).thenReturn(HttpResponse.notFound())

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.findAll(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("O cliente não existe na base do Itaú.", status.description)
        }
    }

    @Test
    fun `should return empty list when client does not have keys on database`() {
        val clientId = UUID.randomUUID().toString()
        val request = FindAllKeysRequest.newBuilder()
                .setClientId(clientId)
                .build()

        Mockito.`when`(itauClient.findClientInfos(clientId)).thenReturn(HttpResponse.ok())

        val response = assertDoesNotThrow {
            grpcClient.findAll(request)
        }

        assertNotNull(response)
        assertEquals(response.keysCount, 0)
    }

    @Test
    fun `should return a list with keys of client specified`() {
        val clientId = UUID.randomUUID().toString()
        val pix1 = createDynamicValidKey("11111111111", clientId)
        val pix2 = createDynamicValidKey("22222222222", clientId)
        val pix3 = createDynamicValidKey("33333333333", clientId)
        repository.save(pix1)
        repository.save(pix2)
        repository.save(pix3)

        val request = FindAllKeysRequest.newBuilder()
                .setClientId(clientId)
                .build()

        Mockito.`when`(itauClient.findClientInfos(clientId)).thenReturn(HttpResponse.ok())

        val response = assertDoesNotThrow {
            grpcClient.findAll(request)
        }

        assertNotNull(response)
        assertEquals(response.keysCount, 3)
        assertEquals("11111111111", response.getKeys(0).key)
        assertEquals("22222222222", response.getKeys(1).key)
        assertEquals("33333333333", response.getKeys(2).key)
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
            AllKeysServiceGrpc.AllKeysServiceBlockingStub {
        return AllKeysServiceGrpc.newBlockingStub(channel)
    }
}