package br.com.zupacademy.pix.details

import br.com.zupacademy.DeleteKeyRequest
import br.com.zupacademy.DeleteKeyServiceGrpc
import br.com.zupacademy.DetailsKeyServiceGrpc
import br.com.zupacademy.KeyDetailRequest
import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.pix.factory.createValidKey
import br.com.zupacademy.shared.httpclients.BacenClient
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
internal class KeyDetailsTestIT(val repository: KeyRepository,
                                val grpcClient: DetailsKeyServiceGrpc.DetailsKeyServiceBlockingStub,
                                val bacenClient: BacenClient
) {

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }

    @Nested
    inner class ByPixIdAndClientId {

        @Test
        fun `should return key details from internal system when client id and pix id is well formed`() {
            val pix = createValidKey()
            repository.save(pix)

            Mockito.`when`(bacenClient.findByKey(pix.key)).thenReturn(HttpResponse.ok())

            val request = KeyDetailRequest.newBuilder()
                    .setPixId(KeyDetailRequest.PixIdAndClientId.newBuilder()
                            .setPixId(pix.id)
                            .setClientId(pix.account.ownerId))
                    .build()

            val response = assertDoesNotThrow {
                grpcClient.keyDetail(request)
            }

            assertNotNull(response)
            assertEquals(pix.key, response.key)
            assertEquals(pix.account.ownerId, response.clientId)
        }

        @Test
        fun `should throw exception when pix Id is not in the database`() {
            val pix = createValidKey()
            repository.save(pix)

            val request = KeyDetailRequest.newBuilder()
                    .setPixId(KeyDetailRequest.PixIdAndClientId.newBuilder()
                            .setPixId(UUID.randomUUID().toString())
                            .setClientId(pix.account.ownerId))
                    .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.keyDetail(request)
            }

            with(error) {
                assertEquals(Status.NOT_FOUND.code, status.code)
                assertEquals("Chave pix não existe no sistema interno.", status.description)
            }
        }

        @Test
        fun `should throw exception when pixIdAndClientId is informed and pixId exists but clientId its not the owner`() {
            val pix = createValidKey()
            repository.save(pix)

            Mockito.`when`(bacenClient.findByKey(pix.key)).thenReturn(HttpResponse.ok())

            val request = KeyDetailRequest.newBuilder()
                    .setPixId(KeyDetailRequest.PixIdAndClientId.newBuilder()
                            .setPixId(pix.id)
                            .setClientId(UUID.randomUUID().toString()))
                    .build()


            val error = assertThrows<StatusRuntimeException> {
                grpcClient.keyDetail(request)
            }

            with(error) {
                assertEquals(Status.PERMISSION_DENIED.code, status.code)
                assertEquals("Chave pix não pertence a esse cliente.", status.description)
            }
        }

        @Test
        fun `should throw exception when key is in the database but its not in bcb system`() {
            val pix = createValidKey()
            repository.save(pix)

            Mockito.`when`(bacenClient.findByKey(pix.key)).thenReturn(HttpResponse.notFound())

            val request = KeyDetailRequest.newBuilder()
                    .setPixId(KeyDetailRequest.PixIdAndClientId.newBuilder()
                            .setPixId(pix.id)
                            .setClientId(pix.account.ownerId))
                    .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.keyDetail(request)
            }

            with(error) {
                assertEquals(Status.NOT_FOUND.code, status.code)
                assertEquals("Chave pix não existe no sistema do Banco Central.", status.description)
            }
        }
    }

    @Nested
    inner class ByKey {
        @Test
        fun `should return key details from internal system when key is in the database`() {
            val pix = createValidKey()
            repository.save(pix)

            val request = KeyDetailRequest.newBuilder()
                    .setKey(pix.key)
                    .build()

            val response = assertDoesNotThrow {
                grpcClient.keyDetail(request)
            }

            assertNotNull(response)
            assertEquals(pix.key, response.key)
            assertEquals(pix.account.number, response.account.number)
            assertEquals(pix.account.ownerId, response.clientId)
        }

        @Test
        fun `should return key details from bcb system when key is not in the internal system`() {
            val key = "12345678911"
            val bacenKeyDetailsResponse = BacenDetailKeyResponse(
                    key = key,
                    keyType = KeyTypeBacen.CPF,
                    createdAt = LocalDateTime.now(),
                    owner = OwnerResponse(
                            type = PersonTypeBacen.LEGAL_PERSON,
                            taxIdNumber = "12345678911",
                            name = "Elias"
                    ),
                    bankAccount = BankAccountResponse(
                            participant = "607090",
                            branch = "0001",
                            accountNumber = "1234-5",
                            accountType = AccountTypeBacen.CACC
                    )
            )

            Mockito.`when`(bacenClient.findByKey(key)).thenReturn(HttpResponse.ok(bacenKeyDetailsResponse))

            val request = KeyDetailRequest.newBuilder()
                    .setKey(key)
                    .build()

            val response = assertDoesNotThrow {
                grpcClient.keyDetail(request)
            }

            assertNotNull(response)
            assertEquals("12345678911", response.key)
            assertEquals("1234-5", response.account.number)
        }

        @Test
        fun `should throw exception when key is informed but its not in the database or bcb system`() {
            val key = UUID.randomUUID().toString()
            Mockito.`when`(bacenClient.findByKey(key)).thenReturn(HttpResponse.notFound())

            val request = KeyDetailRequest.newBuilder()
                    .setKey(key)
                    .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.keyDetail(request)
            }

            with(error) {
                assertEquals(Status.NOT_FOUND.code, status.code)
                assertEquals("Chave Pix não consta no sistema do banco central.", status.description)
            }
        }
    }

    @Nested
    inner class InvalidRequest {
        @Test
        fun `should throw exception when neither key or pixId is informed in request`() {
            val request = KeyDetailRequest.newBuilder()
                    .build()

            val error = assertThrows<StatusRuntimeException> {
                grpcClient.keyDetail(request)
            }

            with(error) {
                assertEquals(Status.FAILED_PRECONDITION.code, status.code)
                assertEquals("Informar apenas a chave ou o objeto com client id e pix id.", status.description)
            }
        }
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
            DetailsKeyServiceGrpc.DetailsKeyServiceBlockingStub {
        return DetailsKeyServiceGrpc.newBlockingStub(channel)
    }
}