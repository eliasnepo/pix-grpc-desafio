package br.com.zupacademy.shared.exceptions

data class PermissionDeniedException(val msg: String) : Exception(msg)