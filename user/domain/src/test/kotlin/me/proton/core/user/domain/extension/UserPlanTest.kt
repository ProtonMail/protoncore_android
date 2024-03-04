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
        every { services } returns USER_SERVICE_MASK_MAIL
        every { subscribed } returns USER_SERVICE_MASK_MAIL
    }

    private val userVpn = mockk<User> {
        every { services } returns USER_SERVICE_MASK_VPN
        every { subscribed } returns USER_SERVICE_MASK_VPN
    }

    private val userDrive = mockk<User> {
        every { services } returns USER_SERVICE_MASK_DRIVE
        every { subscribed } returns USER_SERVICE_MASK_DRIVE
    }

    private val userMailAndVpnAndDrive = mockk<User> {
        every { services } returns USER_SERVICE_MASK_MAIL + USER_SERVICE_MASK_VPN + USER_SERVICE_MASK_DRIVE
        every { subscribed } returns USER_SERVICE_MASK_MAIL + USER_SERVICE_MASK_VPN + USER_SERVICE_MASK_DRIVE
    }

    @Test
    fun hasService() = runTest {
        assertFalse(userNone.hasService())
        assertTrue(userMail.hasService())
        assertTrue(userVpn.hasService())
        assertTrue(userDrive.hasService())
        assertTrue(userMailAndVpnAndDrive.hasService())
    }

    @Test
    fun hasServiceForMail() = runTest {
        assertFalse(userNone.hasServiceForMail())
        assertTrue(userMail.hasServiceForMail())
        assertFalse(userVpn.hasServiceForMail())
        assertFalse(userDrive.hasServiceForMail())
        assertTrue(userMailAndVpnAndDrive.hasServiceForMail())
    }

    @Test
    fun hasServiceForVpn() = runTest {
        assertFalse(userNone.hasServiceForVpn())
        assertFalse(userMail.hasServiceForVpn())
        assertTrue(userVpn.hasServiceForVpn())
        assertFalse(userDrive.hasServiceForVpn())
        assertTrue(userMailAndVpnAndDrive.hasServiceForVpn())
    }

    @Test
    fun hasServiceForDrive() = runTest {
        assertFalse(userNone.hasServiceForDrive())
        assertFalse(userMail.hasServiceForDrive())
        assertFalse(userVpn.hasServiceForDrive())
        assertTrue(userDrive.hasServiceForDrive())
        assertTrue(userMailAndVpnAndDrive.hasServiceForDrive())
    }

    @Test
    fun hasSubscription() = runTest {
        assertFalse(userNone.hasSubscription())
        assertTrue(userMail.hasSubscription())
        assertTrue(userVpn.hasSubscription())
        assertTrue(userDrive.hasSubscription())
        assertTrue(userMailAndVpnAndDrive.hasSubscription())
    }

    @Test
    fun hasSubscriptionForMail() = runTest {
        assertFalse(userNone.hasSubscriptionForMail())
        assertTrue(userMail.hasSubscriptionForMail())
        assertFalse(userVpn.hasSubscriptionForMail())
        assertFalse(userDrive.hasSubscriptionForMail())
        assertTrue(userMailAndVpnAndDrive.hasSubscriptionForMail())
    }

    @Test
    fun hasSubscriptionForVpn() = runTest {
        assertFalse(userNone.hasSubscriptionForVpn())
        assertFalse(userMail.hasSubscriptionForVpn())
        assertTrue(userVpn.hasSubscriptionForVpn())
        assertFalse(userDrive.hasSubscriptionForVpn())
        assertTrue(userMailAndVpnAndDrive.hasSubscriptionForVpn())
    }

    @Test
    fun hasSubscriptionForDrive() = runTest {
        assertFalse(userNone.hasSubscriptionForDrive())
        assertFalse(userMail.hasSubscriptionForDrive())
        assertFalse(userVpn.hasSubscriptionForDrive())
        assertTrue(userDrive.hasSubscriptionForDrive())
        assertTrue(userMailAndVpnAndDrive.hasSubscriptionForDrive())
    }
}
