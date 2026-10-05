package com.learner.invoicegenerator.utils

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 65_536
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val PREFIX = "pbkdf2"

    fun hash(password: String): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)

        val hash = computeHash(password, salt)

        val encodedSalt = Base64.getEncoder().withoutPadding().encodeToString(salt)
        val encodedHash = Base64.getEncoder().withoutPadding().encodeToString(hash)

        return "$PREFIX:$encodedSalt:$encodedHash"
    }

    fun verify(password: String, storedHash: String): Boolean {
        val parts = storedHash.split(":")
        if (parts.size != 3 || parts[0] != PREFIX) {
            return false
        }

        val salt = Base64.getDecoder().decode(parts[1])
        val hash = Base64.getDecoder().decode(parts[2])

        val computedHash = computeHash(password, salt)

        return hash.contentEquals(computedHash)
    }

    private fun computeHash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
}
