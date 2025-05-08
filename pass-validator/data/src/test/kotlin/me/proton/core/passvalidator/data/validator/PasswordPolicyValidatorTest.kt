package me.proton.core.passvalidator.data.validator

import me.proton.core.domain.type.IntEnum
import me.proton.core.passvalidator.data.api.response.toPasswordPolicyState
import me.proton.core.passvalidator.data.entity.PasswordPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PasswordPolicyValidatorTest {
    @Test
    fun `policy with enabled state`() {
        val policy = PasswordPolicy(
            name = "policy_1",
            state = IntEnum(1, 1.toPasswordPolicyState()),
            requirementMessage = "Requirement",
            errorMessage = "Error",
            regex = Regex("[0-9]"),
            hideIfValid = false
        )
        val validator = PasswordPolicyValidator(policy)
        validator.validate("password123").let {
            assertEquals(true, it.isValid)
            assertFalse(it.isOptional)
            assertEquals(policy.hideIfValid, it.hideIfValid)
            assertEquals(policy.requirementMessage, it.requirementMessage)
        }
        assertEquals(false, validator.validate("password").isValid)
    }

    @Test
    fun `policy with optional state`() {
        val policy = PasswordPolicy(
            name = "policy_1",
            state = IntEnum(2, 2.toPasswordPolicyState()),
            requirementMessage = "Requirement",
            errorMessage = "Error",
            regex = Regex("[0-9]"),
            hideIfValid = false
        )
        val validator = PasswordPolicyValidator(policy)
        validator.validate("password").let {
            assertEquals(false, it.isValid)
            assertTrue(it.isOptional)
        }
        validator.validate("password123").let {
            assertEquals(true, it.isValid)
            assertTrue(it.isOptional)
        }
    }

    @Test
    fun `policy with disabled state`() {
        val policy = PasswordPolicy(
            name = "policy_1",
            state = IntEnum(0, 0.toPasswordPolicyState()),
            requirementMessage = "Requirement",
            errorMessage = "Error",
            regex = Regex("[0-9]"),
            hideIfValid = false
        )
        val validator = PasswordPolicyValidator(policy)
        validator.validate("password").let {
            assertNull(it.isValid)
            assertFalse(it.isOptional)
        }
        validator.validate("password123").let {
            assertNull(it.isValid)
            assertFalse(it.isOptional)
        }
    }
}