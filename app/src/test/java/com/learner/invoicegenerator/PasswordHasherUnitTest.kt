package com.learner.invoicegenerator

import com.learner.invoicegenerator.utils.PasswordHasher
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherUnitTest {

    @Test
    fun passwordHasher_hashAndVerify_validPassword() {
        val rawPassword = "SecurePassword123!"
        val hash = PasswordHasher.hash(rawPassword)

        assertTrue(PasswordHasher.verify(rawPassword, hash))
        assertFalse(PasswordHasher.verify("WrongPassword", hash))
    }

    @Test
    fun passwordHasher_handlesSpecialCharsAndEmojis() {
        val rawPassword = "🔑Emoji&SpecialChars#2026!"
        val hash = PasswordHasher.hash(rawPassword)

        assertTrue(PasswordHasher.verify(rawPassword, hash))
        assertFalse(PasswordHasher.verify("🔑Emoji&SpecialChars#2026", hash))
    }
}
