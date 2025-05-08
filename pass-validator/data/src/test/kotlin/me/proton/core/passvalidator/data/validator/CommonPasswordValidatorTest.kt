package me.proton.core.passvalidator.data.validator

import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class CommonPasswordValidatorTest {
    @Test
    fun `password is common`() {
        val validator = CommonPasswordValidator(
            context = mockk(relaxed = true),
            hideIfValid = false,
            isPasswordCommon = { true },
            isOptional = false
        )
        assertEquals(false, validator.validate("password").isValid)
    }

    @Test
    fun `password is not common`() {
        val validator = CommonPasswordValidator(
            context = mockk(relaxed = true),
            hideIfValid = false,
            isPasswordCommon = { false },
            isOptional = false
        )
        assertEquals(true, validator.validate("password").isValid)
    }
}
