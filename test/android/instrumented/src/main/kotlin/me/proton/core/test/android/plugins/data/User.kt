/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.test.android.plugins.data

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.serialization.Serializable
import me.proton.core.test.android.instrumented.utils.StringUtils.randomString
import me.proton.core.util.kotlin.deserializeList

@Serializable
data class User(
    var name: String = "proton_core_${randomString(stringLength = 4)}",
    var password: String = "12345678",
    val email: String = "",

    val passphrase: String = "",
    val twoFa: String = "",

    val firstName: String = "",
    val lastName: String = "",
    val verificationEmail: String = "",
    val phone: String = "",
    val country: String = "",

    val plan: Plan = Plan.Free,
    val cards: List<Card> = emptyList(),
    val paypal: String = "",
) {

    val isOnePasswordWithUsername: Boolean = passphrase.isEmpty() && twoFa.isEmpty() && name.isNotEmpty()

    val isPaid: Boolean = plan != Plan.Free

    class Users(private val jsonPath: String) {

        private val userData: MutableList<User> = InstrumentationRegistry
            .getInstrumentation()
            .context
            .assets
            .open(jsonPath)
            .bufferedReader()
            .use { it.readText() }
            .deserializeList<User>() as MutableList<User>

        fun getUser(usernameAndOnePass: Boolean = true, predicate: (User) -> Boolean = { true }): User {
            userData
                .filter { it.isOnePasswordWithUsername == usernameAndOnePass }
                .filter(predicate)
                .let {
                    try {
                        return it.random()
                    } catch (e: NoSuchElementException) {
                        throw NoSuchElementException("User does not exist in assets/$jsonPath")
                    }
                }
        }
    }
}
