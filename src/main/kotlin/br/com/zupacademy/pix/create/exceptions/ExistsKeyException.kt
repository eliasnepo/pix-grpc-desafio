package br.com.zupacademy.pix.create.exceptions

data class ExistsKeyException(val msg: String) : Exception(msg)