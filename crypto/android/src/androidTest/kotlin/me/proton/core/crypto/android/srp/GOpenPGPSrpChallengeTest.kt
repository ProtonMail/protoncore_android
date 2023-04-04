/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.crypto.android.srp

import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertFails

internal class GOpenPGPSrpChallengeTest {

    private val challenge = GOpenPGPSrpChallenge()

    @Test
    fun testArgon2PreimageChallenge() = runTest {
        val b64Challenge = "qbYJSn07JQGfol0u8MJTZ16fDRyFo2AR6phcgqlZCr44RBpz/odJc17EROMfMOpz2dE8oHW2JHeqoRax2ha4bpGusDBkEySSWJU+cmuWePzUC58fTY+VJMLBMDLhdqV9QKvozeqKcoPzqDoHZZYmyWQf4DIAKfgaha/WwzMikQMBAAAAIAAAAOEQAAABAAAA"
        val actual = challenge.argon2PreimageChallenge(b64Challenge)
        var expected = "ewAAAAAAAABXe+n/4g0Hfz40eEw7h5d3XeiKdWilfCJvz0izj7p0YA=="
        assertEquals(expected, actual)
    }

    @Test
    fun testEcdlpChallenge() = runTest {
        val challenge = "kavkPtdQF/bQMvMlCjfgMdRdMsIsA8DP0X0/p44n+6jcchSeEewrjqcwy0FYF0jkWO1Wz1pdSe3meRNtpf+g2DQluiIbobuq4mM7J45fabUlKRtbEhSogoc9H3S74Wlj"
        val expected = "ngAAAAAAAAAczZrEZLqS9+TGdB7vNex1HzvPpFJD7Qd4+yPEgGduDw=="
        val actual = GOpenPGPSrpChallenge().ecdlpChallenge(challenge)
        assertEquals(expected, actual)
    }

    @Test
    fun testArgon2PreimageChallengeWithInvalidInput() = runTest {
        val invalidB64Challenge = "11123123144444"
        assertFails { challenge.argon2PreimageChallenge(invalidB64Challenge) }
    }

    @Test
    fun testEcdlpChallengeWithEmptyInput() = runTest {
        assertFails { challenge.ecdlpChallenge("") }
    }
}