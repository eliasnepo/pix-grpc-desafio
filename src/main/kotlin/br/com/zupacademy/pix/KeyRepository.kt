package br.com.zupacademy.pix

import br.com.zupacademy.pix.Key
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface KeyRepository : JpaRepository<Key, String> {
    fun existsByKey(key: String): Boolean
}