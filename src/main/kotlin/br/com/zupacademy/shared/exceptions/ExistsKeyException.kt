package br.com.zupacademy.shared.exceptions

data class ExistsKeyException(val msg: String) : Exception(msg)