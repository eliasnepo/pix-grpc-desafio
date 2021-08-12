package br.com.zupacademy.pix.create.validations

import br.com.zupacademy.pix.create.exceptions.StatusWithDetails

interface ExceptionHandler<E : Exception> {

    fun handle(e: E): StatusWithDetails
    fun supports(e: Exception): Boolean
}