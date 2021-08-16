package br.com.zupacademy.shared.httpclients.dto

import java.time.LocalDateTime

data class BacenDeleteKeyResponse(
        val key: String,
        val participant: String,
        val deletedAt: LocalDateTime
)