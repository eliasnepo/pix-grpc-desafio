package br.com.zupacademy.pix.create

import br.com.zupacademy.AccountType
import br.com.zupacademy.KeyRequest
import br.com.zupacademy.KeyType
import br.com.zupacademy.PixKeyServiceGrpc
import br.com.zupacademy.pix.create.exceptions.StatusWithDetails
import br.com.zupacademy.pix.create.model.Account
import br.com.zupacademy.pix.create.model.Key
import br.com.zupacademy.pix.create.repository.KeyRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CreateKeyEndpointTestIT(
    val keyRepository: KeyRepository,
    val grpcClient: PixKeyServiceGrpc.PixKeyServiceBlockingStub
) {

    @BeforeEach
    fun setUp() {
        keyRepository.deleteAll()
    }

    @Test
    fun `should add pix key when valid data`() {
        val request = KeyRequest.newBuilder()
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setKeyType(KeyType.EMAIL)
            .setKey("rafa@zup.com.br")
            .setAccountType(AccountType.CONTA_CORRENTE)
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
            .setClientId("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setKeyType(KeyType.EMAIL)
            .setKey("rafa@zup.com.br")
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
            .setClientId("batata")
            .setKeyType(KeyType.EMAIL)
            .setKey("rafa@zup.com.br")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()

        val error = assertThrows<StatusRuntimeException> {
            grpcClient.generateKey(request)
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("O cliente não está na base do Itau.", status.description)
        }
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

@Factory
fun createValidKey(): Key {
    return Key(key = "rafa@zup.com.br", keyType = br.com.zupacademy.pix.create.model.enums.KeyType.EMAIL,
        Account("0001", "1234", accountType = br.com.zupacademy.pix.create.model.enums.AccountType.CONTA_CORRENTE,
            ownerName = "Rafael"))
}