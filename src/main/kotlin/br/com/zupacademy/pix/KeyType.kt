package br.com.zupacademy.pix

enum class KeyType {
    CPF {
        override fun validate(key: String): Boolean {
            if (key.isNullOrBlank()) {
                return false
            }
            return key.matches("^[0-9]{11}\$".toRegex())
        }
    },
    EMAIL {
        override fun validate(key: String): Boolean {
            if (key.isNullOrBlank()) {
                return false
            }
            return key.matches("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
                .toRegex())
        }
    },
    PHONE {
        override fun validate(key: String): Boolean {
            if (key.isNullOrBlank()) {
                return false
            }
            return key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    RANDOM {
        override fun validate(key: String): Boolean {
            return key.isNullOrBlank()
        }
    };

    abstract fun validate(key: String): Boolean
}