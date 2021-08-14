package br.com.zupacademy.pix.delete

import br.com.zupacademy.DeleteKeyRequest

fun DeleteKeyRequest.toModel() : DeleteKeyDTO {
    return DeleteKeyDTO(
            clientId = clientId,
            pixId = pixId
    )
}