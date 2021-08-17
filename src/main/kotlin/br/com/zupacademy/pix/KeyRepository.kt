package br.com.zupacademy.pix

import br.com.zupacademy.pix.Key
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface KeyRepository : JpaRepository<Key, String> {
    fun existsByKey(key: String): Boolean
    fun findByKey(key: String): Optional<Key>
}