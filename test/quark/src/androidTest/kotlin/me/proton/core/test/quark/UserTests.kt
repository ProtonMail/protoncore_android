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

package me.proton.core.test.quark

import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.v2.command.userCreate
import org.junit.Test
import kotlin.test.assertEquals

class UserTests : BaseTest() {
    @Test
    fun testUserCreate() {
        val user = User()

        val response = quarkCommand.userCreate(user)

        assertEquals(user.name, response.name)
        assertEquals(user.password, response.password)
        assertEquals(response.statusInfo, "2 (ACTIVE)")
        assertEquals("${user.name}@${host}", response.email)
    }
}