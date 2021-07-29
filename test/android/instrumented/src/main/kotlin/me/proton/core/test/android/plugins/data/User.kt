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
import me.proton.core.test.android.instrumented.utils.StringUtils.getEmailString
import me.proton.core.test.android.instrumented.utils.StringUtils.randomString
import me.proton.core.util.kotlin.deserializeList
import kotlin.random.Random

@Serializable
data class User(
    val name: String = randomString(),
    val password: String = "12345678",
    val email: String = getEmailString(),
    val passphrase: String = "",
    val twoFa: String = "",

    val firstName: String = randomString(),
    val lastName: String = randomString(),
    val verificationEmail: String = getEmailString(),
    val phone: String = "",
    val country: String = "",
    val type: Int = Random.nextInt(1, 2),

    val plan: Plan = Plan.Free,
    val cards: List<Card> = emptyList(),
    val paypal: String = ""
) {

    val isDefault: Boolean = passphrase.isEmpty() && twoFa.isEmpty() && name.isNotEmpty()

    val isPaid: Boolean = plan != Plan.Free

    class Users(jsonPath: String) {

        private val userData: List<User> = InstrumentationRegistry
            .getInstrumentation()
            .context
            .assets
            .open(jsonPath)
            .bufferedReader()
            .use { it.readText() }
            .deserializeList()

        fun getUser(predicate: (User) -> Boolean = { it.isDefault }): User =
            userData.filterTo(ArrayList(), predicate).random()
    }
}
