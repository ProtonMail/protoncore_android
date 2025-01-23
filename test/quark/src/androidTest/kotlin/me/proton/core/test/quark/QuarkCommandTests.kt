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
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.command.expireSession
import me.proton.core.test.quark.v2.command.jailUnban
import me.proton.core.test.quark.v2.command.populateUserWithData
import me.proton.core.test.quark.v2.command.seedSubscriber
import okhttp3.Response
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertEquals

@RunWith(Parameterized::class)
class QuarkCommandTests(
    private val command: QuarkCommand.() -> Response
) : BaseTest() {

    @Test
    fun testCommand() {
        val command = quarkCommand.command()
        assertEquals(command.code, 200, command.message)
    }

    companion object {
        @get:Parameterized.Parameters(name = "{index}")
        @get:JvmStatic
        val data = listOf<QuarkCommand.() -> Response>(
            { jailUnban() },
            { seedSubscriber() },
            { expireSession("pro") },
            { populateUserWithData(User(name = "pro", password = "pro", dataSetScenario = "1")) }
        ).toList()
    }
}
