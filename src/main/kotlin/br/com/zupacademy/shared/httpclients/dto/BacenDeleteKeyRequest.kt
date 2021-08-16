package br.com.zupacademy.shared.httpclients.dto

data class BacenDeleteKeyRequest(
        val key: String,
        val participant: String
)