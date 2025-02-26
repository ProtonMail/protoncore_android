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

package me.proton.core.biometric.data

import me.proton.core.biometric.domain.BiometricAuthenticator.DeviceCredential
import me.proton.core.biometric.domain.BiometricAuthenticator.Strong
import kotlin.test.Test
import kotlin.test.assertEquals

class StrongAuthenticatorsResolverTest {
    @Test
    fun `resolving authenticators`() {
        assertEquals(
            setOf(DeviceCredential, Strong),
            StrongAuthenticatorsResolver(27)(setOf(DeviceCredential))
        )
        assertEquals(
            setOf(Strong),
            StrongAuthenticatorsResolver(28)(setOf(DeviceCredential))
        )
        assertEquals(
            setOf(DeviceCredential),
            StrongAuthenticatorsResolver(30)(setOf(DeviceCredential))
        )
        assertEquals(
            setOf(Strong),
            StrongAuthenticatorsResolver(29)(setOf(DeviceCredential, Strong))
        )
        assertEquals(
            setOf(DeviceCredential, Strong),
            StrongAuthenticatorsResolver(30)(setOf(DeviceCredential, Strong))
        )
    }
}
