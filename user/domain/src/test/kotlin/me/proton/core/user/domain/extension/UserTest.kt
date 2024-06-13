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
import me.proton.core.user.domain.entity.Role
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserTest {

    private val user = mockk<User> {
        every { email } returns null
        every { name } returns "name"
        every { displayName } returns "Name"
        every { keys } returns listOf(mockk(), mockk())
        every { private } returns true
        every { role } returns Role.OrganizationAdmin
        every { type } returns Type.Proton
    }

    private val userNoName = mockk<User> {
        every { email } returns "username@domain.com"
        every { name } returns null
        every { displayName } returns null
        every { keys } returns listOf(mockk(), mockk())
        every { private } returns false
        every { role } returns Role.OrganizationMember
    }

    private val userNoKeys = mockk<User> {
        every { email } returns "username@domain.com"
        every { name } returns null
        every { displayName } returns null
        every { keys } returns emptyList()
        every { private } returns false
        every { role } returns Role.NoOrganization
    }

    private val userCredLess = mockk<User> {
        every { type } returns Type.CredentialLess
    }

    @Test
    fun nameNotNull() = runTest {
        assertEquals(expected = "name", actual = user.nameNotNull())
        assertEquals(expected = "username@domain.com", actual = userNoName.nameNotNull())
    }

    @Test
    fun displayNameNotNull() = runTest {
        assertEquals(expected = "Name", actual = user.displayNameNotNull())
        assertEquals(expected = "username", actual = userNoName.displayNameNotNull())
    }

    @Test
    fun hasKeys() = runTest {
        assertTrue(user.hasKeys())
        assertFalse(userNoKeys.hasKeys())
    }

    @Test
    fun hasUsername() = runTest {
        assertTrue(user.hasUsername())
        assertFalse(userNoName.hasUsername())
    }

    @Test
    fun isPrivate() = runTest {
        assertTrue(user.isPrivate())
        assertFalse(userNoName.isPrivate())
    }

    @Test
    fun isOrganizationAdmin() = runTest {
        assertTrue(user.isOrganizationAdmin())
        assertFalse(userNoName.isOrganizationAdmin())
        assertFalse(userNoKeys.isOrganizationAdmin())
    }

    @Test
    fun isOrganizationMember() = runTest {
        assertFalse(user.isOrganizationMember())
        assertTrue(userNoName.isOrganizationMember())
        assertFalse(userNoKeys.isOrganizationMember())
    }

    @Test
    fun isNotOrganizationUser() = runTest {
        assertFalse(user.isNotOrganizationUser())
        assertFalse(userNoName.isNotOrganizationUser())
        assertTrue(userNoKeys.isNotOrganizationUser())
    }

    @Test
    fun isOrganizationUser() = runTest {
        assertTrue(user.isOrganizationUser())
        assertTrue(userNoName.isOrganizationUser())
        assertFalse(userNoKeys.isOrganizationUser())
    }

    @Test
    fun canReadSubscription() = runTest {
        assertTrue(user.canReadSubscription())
        assertFalse(userNoName.canReadSubscription())
        assertTrue(userNoKeys.canReadSubscription())
    }

    @Test
    fun hasNoBaseOrDriveStoragePercentage() {
        val user = mockk<User> {
            every { usedBaseSpace } returns null
            every { maxBaseSpace } returns null
            every { usedDriveSpace } returns null
            every { maxDriveSpace } returns null
        }
        assertNull(user.getUsedBaseSpacePercentage())
        assertNull(user.getUsedDriveSpacePercentage())
    }

    @Test
    fun hasStoragePercentage() {
        val user = mockk<User> {
            every { usedSpace } returns 1050
            every { maxSpace } returns 1200
            every { usedBaseSpace } returns 50
            every { maxBaseSpace } returns 200
            every { usedDriveSpace } returns 1000
            every { maxDriveSpace } returns 1000
        }
        assertEquals(88, user.getUsedTotalSpacePercentage())
        assertEquals(25, user.getUsedBaseSpacePercentage())
        assertEquals(100, user.getUsedDriveSpacePercentage())
    }

    @Test
    fun usedSpaceIsGreaterThanMaxSpace() {
        val user = mockk<User> {
            every { usedSpace } returns 1250
            every { maxSpace } returns 1000
            every { usedBaseSpace } returns 300
            every { maxBaseSpace } returns 200
            every { usedDriveSpace } returns 101
            every { maxDriveSpace } returns 100
        }
        assertEquals(125, user.getUsedTotalSpacePercentage())
        assertEquals(150, user.getUsedBaseSpacePercentage())
        assertEquals(101, user.getUsedDriveSpacePercentage())
    }

    @Test
    fun credentialLessUser() {
        assertTrue(userCredLess.isCredentialLess())
        assertFalse(user.isCredentialLess())
    }
}
