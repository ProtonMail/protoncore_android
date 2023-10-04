/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.key.domain.extension

import me.proton.core.key.domain.entity.key.PublicKey
import me.proton.core.key.domain.entity.key.PublicKeyRing
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PublicKeyRingKtTest {

    @Test
    fun `PublicKeyRing_allowCompromisedKeys`() {
        val publicKeyRing = PublicKeyRing(
            keys = listOf(
                PublicKey(
                    key = "test-key1",
                    isPrimary = false,
                    isActive = true,
                    canEncrypt = true,
                    canVerify = false
                ),
                PublicKey(
                    key = "test-key2",
                    isPrimary = true,
                    isActive = false,
                    canEncrypt = true,
                    canVerify = false
                )
            )
        )

        val result = publicKeyRing.allowCompromisedKeys()
        assertEquals(2, result.keys.size)
        assertTrue(result.keys[0].canVerify)
        assertFalse(result.keys[1].canVerify)
    }
}
