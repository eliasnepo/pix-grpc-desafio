package br.com.zupacademy.pix.details

import br.com.zupacademy.KeyDetailRequest
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun KeyDetailRequest.toModel(validator: Validator) : Filter {

    val filter = when(filterCase!!) {
        KeyDetailRequest.FilterCase.PIXID -> pixId.let {
            Filter.ByPixId(clientId = it.clientId, pixId = it.pixId)
        }
        KeyDetailRequest.FilterCase.KEY -> Filter.ByKey(key)
        KeyDetailRequest.FilterCase.FILTER_NOT_SET -> throw IllegalStateException("Informar apenas a chave ou o objeto com client id e pix id.")
    }

    val violations = validator.validate(filter)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations);
    }

    return filter
}