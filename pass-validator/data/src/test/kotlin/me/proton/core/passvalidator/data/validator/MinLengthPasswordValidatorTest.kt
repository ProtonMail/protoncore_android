package me.proton.core.passvalidator.data.validator

import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class MinLengthPasswordValidatorTest {
    @Test
    fun `password is too short`() {
        val validator = MinLengthPasswordValidator(
            context = mockk(relaxed = true),
            hideIfValid = false,
            minLength = 8,
            isOptional = false
        )
        assertEquals(false, validator.validate("").isValid)
        assertEquals(false, validator.validate("short").isValid)
        assertEquals(false, validator.validate("short12").isValid)
    }

    @Test
    fun `password is long enough`() {
        val validator = MinLengthPasswordValidator(
            context = mockk(relaxed = true),
            hideIfValid = false,
            minLength = 8,
            isOptional = false
        )
        assertEquals(true, validator.validate("password").isValid)
        assertEquals(true, validator.validate("password123").isValid)
    }
}
