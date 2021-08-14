package br.com.zupacademy.pix.create

import br.com.zupacademy.pix.KeyType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class KeyTypeValidationTest {

    @Nested
    inner class CPF {

        @Test
        fun `should return true when valid CPF`() {
            val validCpf = "12345678911"

            assertEquals(11, validCpf.length)
            assertTrue(KeyType.CPF.validate(validCpf))
        }

        @Test
        fun `should return false when empty CPF`() {
            val invalidCpf = ""
            assertFalse(KeyType.CPF.validate(invalidCpf))
        }

        @Test
        fun `should return false when invalid CPF`() {
            val invalidCpf = "1234567890"
            val invalidCpf2 = "123456789111"

            assertFalse(KeyType.CPF.validate(invalidCpf))
            assertFalse(KeyType.CPF.validate(invalidCpf2))
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `should return true when valid email`() {
            val validEmail = "elias@zup.com"
            val validEmail2 = "elias@zup.com.br"

            assertTrue(KeyType.EMAIL.validate(validEmail))
            assertTrue(KeyType.EMAIL.validate(validEmail2))
        }

        @Test
        fun `should return false when empty email`() {
            val invalidEmail = ""
            assertFalse(KeyType.EMAIL.validate(invalidEmail))
        }

        @Test
        fun `should return false when invalid email`() {
            val invalidEmail = "elias@zup"
            val invalidEmail2 = "elias.zup.br"

            assertFalse(KeyType.EMAIL.validate(invalidEmail))
            assertFalse(KeyType.EMAIL.validate(invalidEmail2))
        }
    }

    @Nested
    inner class PHONE {
        @Test
        fun `should return true when valid phone`() {
            val validPhone = "+55999999999"

            assertTrue(KeyType.PHONE.validate(validPhone))
        }

        @Test
        fun `should return false when phone is empty`() {
            val invalidPhone = ""

            assertFalse(KeyType.PHONE.validate(invalidPhone))
        }

        @Test
        fun `should return false when invalid phone`() {
            val invalidPhone = "999999999"
            val invalidPhone2 = "+55999999999999999"

            assertFalse(KeyType.PHONE.validate(invalidPhone))
            assertFalse(KeyType.PHONE.validate(invalidPhone2))
        }
    }

    @Nested
    inner class RANDOM {
        @Test
        fun `should return false when its not empty`() {
            assertFalse(KeyType.RANDOM.validate("a"))
        }

        @Test
        fun `should return true when is empty`() {
            assertTrue(KeyType.RANDOM.validate(""))
        }
    }
}