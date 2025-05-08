/*
 * Copyright (c) 2025 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.passvalidator.data.api.response

import me.proton.core.domain.type.IntEnum
import me.proton.core.passvalidator.data.entity.PasswordPolicyState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PasswordPolicyResourceMappingTest {
    @Test
    fun `password policy resource mapping`() {
        val policy = PasswordPolicyResource(
            name = "Policy 1",
            state = 1,
            requirementMessage = "This is required",
            errorMessage = "This is error",
            regex = "[0-9]",
            hideIfValid = true
        ).toPasswordPolicy()

        assertEquals("Policy 1", policy.name)
        assertEquals(IntEnum(1, PasswordPolicyState.Enabled), policy.state)
        assertEquals("This is required", policy.requirementMessage)
        assertEquals("This is error", policy.errorMessage)
        assertEquals("[0-9]", policy.regex?.pattern)
        assertNotNull(policy.regex?.find("1450"))
        assertEquals(true, policy.hideIfValid)
    }

    @Test
    fun `invalid regex`() {
        val policy = PasswordPolicyResource(
            name = "Policy 1",
            state = 1,
            requirementMessage = "This is required",
            errorMessage = "This is error",
            regex = "[0-9", // missing closing bracket ']'
            hideIfValid = false
        ).toPasswordPolicy()

        assertEquals(IntEnum(1, PasswordPolicyState.InvalidRegex), policy.state)
        assertNull(policy.regex)
    }
}
