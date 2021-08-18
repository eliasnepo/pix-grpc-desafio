package br.com.zupacademy.pix.listAll

import br.com.zupacademy.*
import br.com.zupacademy.pix.KeyRepository
import br.com.zupacademy.shared.exceptions.ResourceNotFoundException
import br.com.zupacademy.shared.httpclients.ItauClient
import br.com.zupacademy.shared.validations.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import java.lang.IllegalArgumentException
import java.time.ZoneOffset
import javax.inject.Singleton

@ErrorHandler
@Singleton
class FindAllKeysEndpoint(val keyRepository: KeyRepository, val itauClient: ItauClient) : AllKeysServiceGrpc.AllKeysServiceImplBase() {

    override fun findAll(request: FindAllKeysRequest, responseObserver: StreamObserver<FindAllKeysResponse>) {
        if (request.clientId.isNullOrBlank()) {
            throw IllegalArgumentException("O id do cliente deve estar preenchido.")
        }

        if (itauClient.findClientInfos(request.clientId).status == HttpStatus.NOT_FOUND) {
            throw ResourceNotFoundException("O cliente não existe na base do Itaú.")
        }

        val keys = keyRepository.findByAccountOwnerId(request.clientId)
                .map { key -> FindAllKeysResponse.Key.newBuilder()
                        .setKey(key.key)
                        .setPixId(key.id)
                        .setClientId(key.account.ownerId)
                        .setKeyType(KeyType.valueOf(key.keyType.name))
                        .setAccountType(AccountType.valueOf(key.account.accountType.name))
                        .setCreatedAt(key.createdAt.let {
                            Timestamp.newBuilder()
                                    .setSeconds(it.toInstant(ZoneOffset.UTC).epochSecond)
                                    .setNanos(it.toInstant(ZoneOffset.UTC).nano)
                                    .build()
                        })
                        .build()}

        val keyResponse = FindAllKeysResponse.newBuilder()
                .addAllKeys(keys)
                .build()

        responseObserver.onNext(keyResponse)
        responseObserver.onCompleted()
    }
}