package br.com.zupacademy.shared.exceptions

data class ResourceNotFoundException(val msg: String) : Exception(msg)