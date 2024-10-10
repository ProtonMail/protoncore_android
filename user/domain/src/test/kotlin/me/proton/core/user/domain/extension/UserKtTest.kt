/*
 * Copyright (c) 2024 Proton Technologies AG
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

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.Type
import me.proton.core.user.domain.entity.User
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserKtTest {

    private val userId = UserId("test-user-id")
    private val user = User(
        userId = userId,
        email = null,
        name = "test username",
        displayName = null,
        currency = "test-curr",
        credit = 0,
        createdAtUtc = 1000L,
        usedSpace = 0,
        maxSpace = 100,
        maxUpload = 100,
        role = null,
        private = true,
        services = 1,
        subscribed = 0,
        delinquent = null,
        recovery = null,
        keys = emptyList(),
        flags = emptyMap(),
        type = Type.Proton
    )

    @Test
    fun getInitialsDefault() {
        val initials = user.getInitials()
        assertEquals("TU", initials)
    }

    @Test
    fun getInitialsUserThreeNamesDefaultThree() {
        val initials = user.copy(name = "Test Username User").getInitials(count = 3)
        assertEquals("TUU", initials)
    }

    @Test
    fun getInitialsDefaultTwoNamesThreeLetters() {
        val initials = user.getInitials(count = 3)
        assertEquals("TU", initials)
    }

    @Test
    fun getInitialsDisplayName() {
        val initials = user.copy(name = null, displayName = "Display Name").getInitials()
        assertEquals("DN", initials)
    }

    @Test
    fun getInitialsEmail() {
        val initials = user.copy(name = null, displayName = null, email = "test.email@testemail.org").getInitials()
        assertEquals("TE", initials)
    }

    @Test
    fun getInitialsDefaultOneLetter() {
        val initials = user.getInitials(count = 1)
        assertEquals("T", initials)
    }

    @Test
    fun getInitialsNull() {
        val initials = user.copy(name = null, displayName = null, email = null).getInitials()
        assertNull(initials)
    }
}