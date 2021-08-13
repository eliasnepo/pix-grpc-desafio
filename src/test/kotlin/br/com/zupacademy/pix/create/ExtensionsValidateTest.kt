package br.com.zupacademy.pix.create

import br.com.zupacademy.AccountType
import br.com.zupacademy.KeyRequest
import br.com.zupacademy.KeyType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ExtensionsValidateTest {

    @Test
    fun `should do nothing when key type is random and key is blank`() {
        var keyRequest = KeyRequest.newBuilder()
            .setKeyType(KeyType.RANDOM)
            .setKey("")
            .setClientId("32123")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()

        assertDoesNotThrow {
            keyRequest.validate()
        }
        assertEquals("", keyRequest.key)
    }

    @Test
    fun `should throws exception when key type is random and its fully`() {
        val keyRequest = KeyRequest.newBuilder()
            .setKeyType(KeyType.RANDOM)
            .setKey("acbc")
            .setClientId("32123")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()

        assertNotSame("", keyRequest.key)
        assertThrows<IllegalArgumentException> {
            keyRequest.validate()
        }
    }

    @Test
    fun `should throws exception when key is empty and key type its not random`() {
        val keyRequest = KeyRequest.newBuilder()
            .setKeyType(KeyType.CPF)
            .setClientId("32123")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()

        assertEquals("", keyRequest.key)
        assertThrows<IllegalArgumentException> {
            keyRequest.validate()
        }
    }

    @Test
    fun `should throws exception when clientId is empty`() {
        val keyRequest = KeyRequest.newBuilder()
            .setKeyType(KeyType.CPF)
            .setKey("abc")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()

        assertEquals("", keyRequest.clientId)
        assertThrows<IllegalArgumentException> {
            keyRequest.validate()
        }
    }

    @Test
    fun `should throw exception when accountType is unknown`() {
        val keyRequest = KeyRequest.newBuilder()
            .setKey("abc")
            .setKeyType(KeyType.CPF)
            .setClientId("cliente")
            .build()

        assertEquals(AccountType.UNKNOWN_ACCOUNT, keyRequest.accountType)
        assertThrows<IllegalArgumentException> {
            keyRequest.validate()
        }
    }

    @Test
    fun `should throw exception when keyType is unknown`() {
        val keyRequest = KeyRequest.newBuilder()
            .setKey("abc")
            .setClientId("cliente")
            .setAccountType(AccountType.CONTA_CORRENTE)
            .build()

        assertEquals(KeyType.UNKNOWN_KEY, keyRequest.keyType)
        assertThrows<IllegalArgumentException> {
            keyRequest.validate()
        }
    }
}