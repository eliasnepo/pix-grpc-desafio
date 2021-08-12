package br.com.zupacademy.pix.create.exceptions

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto

data class StatusWithDetails(val status: Status, val metadata: Metadata = Metadata()) {

    constructor(se: StatusRuntimeException): this(se.status, se.trailers ?: Metadata())

    constructor(sp: com.google.rpc.Status): this(StatusProto.toStatusRuntimeException(sp))

    fun asRuntimeException(): StatusRuntimeException {
        return status.asRuntimeException(metadata)
    }
}