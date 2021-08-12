package br.com.zupacademy.pix.create.exceptions

data class ResourceNotFoundException(val msg: String) : Exception(msg)