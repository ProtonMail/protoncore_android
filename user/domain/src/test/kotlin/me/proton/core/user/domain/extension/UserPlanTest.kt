/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.user.domain.extension

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.user.domain.entity.User
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserPlanTest {

    private val userNone = mockk<User> {
        every { services } returns 0
        every { subscribed } returns 0
    }

    private val userMail = mockk<User> {
        every { services } returns MASK_MAIL
        every { subscribed } returns MASK_MAIL
    }

    private val userVpn = mockk<User> {
        every { services } returns MASK_VPN
        every { subscribed } returns MASK_VPN
    }

    private val userMailAndVpn = mockk<User> {
        every { services } returns MASK_MAIL + MASK_VPN
        every { subscribed } returns MASK_MAIL + MASK_VPN
    }

    @Test
    fun hasService() = runTest {
        assertFalse(userNone.hasService())
        assertTrue(userMail.hasService())
        assertTrue(userVpn.hasService())
        assertTrue(userMailAndVpn.hasService())
    }

    @Test
    fun hasServiceForMail() = runTest {
        assertFalse(userNone.hasServiceForMail())
        assertTrue(userMail.hasServiceForMail())
        assertFalse(userVpn.hasServiceForMail())
        assertTrue(userMailAndVpn.hasServiceForMail())
    }

    @Test
    fun hasServiceForVpn() = runTest {
        assertFalse(userNone.hasServiceForVpn())
        assertFalse(userMail.hasServiceForVpn())
        assertTrue(userVpn.hasServiceForVpn())
        assertTrue(userMailAndVpn.hasServiceForVpn())
    }

    @Test
    fun hasSubscription() = runTest {
        assertFalse(userNone.hasSubscription())
        assertTrue(userMail.hasSubscription())
        assertTrue(userVpn.hasSubscription())
        assertTrue(userMailAndVpn.hasSubscription())
    }

    @Test
    fun hasSubscriptionForMail() = runTest {
        assertFalse(userNone.hasSubscriptionForMail())
        assertTrue(userMail.hasSubscriptionForMail())
        assertFalse(userVpn.hasSubscriptionForMail())
        assertTrue(userMailAndVpn.hasSubscriptionForMail())
    }

    @Test
    fun hasSubscriptionForVpn() = runTest {
        assertFalse(userNone.hasSubscriptionForVpn())
        assertFalse(userMail.hasSubscriptionForVpn())
        assertTrue(userVpn.hasSubscriptionForVpn())
        assertTrue(userMailAndVpn.hasSubscriptionForVpn())
    }
}
