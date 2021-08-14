package br.com.zupacademy.shared.validations

import br.com.zupacademy.shared.exceptions.StatusWithDetails

interface ExceptionHandler<E : Exception> {

    fun handle(e: E): StatusWithDetails
    fun supports(e: Exception): Boolean
}