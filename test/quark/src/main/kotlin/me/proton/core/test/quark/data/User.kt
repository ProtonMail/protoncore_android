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

package me.proton.core.test.quark.data

import kotlinx.serialization.Serializable
import me.proton.core.test.quark.util.randomString
import me.proton.core.util.kotlin.EMPTY_STRING
import me.proton.core.util.kotlin.deserializeList
import me.proton.core.util.kotlin.random

@Serializable
public data class User(
    val addressID: String = EMPTY_STRING,
    val authVersion: Int = -1,
    val id: String = EMPTY_STRING,
    val decryptedUserId: Long = 0L,
    val name: String = randomUsername(),
    val password: String = randomString(length = 8),
    val email: String = EMPTY_STRING,
    val externalEmail: String = EMPTY_STRING,
    val status: Int = -1,

    val passphrase: String = EMPTY_STRING,
    val twoFa: String = EMPTY_STRING,

    val phone: String = EMPTY_STRING,
    val country: String = EMPTY_STRING,

    val plan: Plan = Plan.Free,
    val cards: List<Card> = emptyList(),
    val paypal: String = EMPTY_STRING,
    val dataSetScenario: String = EMPTY_STRING,
    val recoveryEmail: String = EMPTY_STRING,
    val recoveryPhone: String = EMPTY_STRING,

    val isExternal: Boolean = false
) {

    val isOnePasswordWithUsername: Boolean = passphrase.isEmpty() && twoFa.isEmpty() && name.isNotEmpty()

    val isPaid: Boolean = plan != Plan.Free

    public companion object {
        public fun randomUsername(): String = "proton_core_${String.random()}"
    }

    public class Users constructor(private val userData: List<User>) {
        public fun getUser(usernameAndOnePass: Boolean = true, predicate: (User) -> Boolean = { true }): User {
            getUsers(usernameAndOnePass, predicate)
                .let {
                    try {
                        return it.random()
                    } catch (e: NoSuchElementException) {
                        println("User does not exist.")
                        throw e
                    }
                }
        }

        public fun getUsers(usernameAndOnePass: Boolean = true, predicate: (User) -> Boolean = { true }): List<User> {
            return userData
                .filter { it.isOnePasswordWithUsername == usernameAndOnePass }
                .filter(predicate)
        }

        public companion object {
            public fun fromJson(json: String): Users =
                Users(json.deserializeList())

            public fun fromJavaResources(classLoader: ClassLoader, resourcePath: String): Users =
                fromJson(classLoader
                    .getResourceAsStream(resourcePath)
                    .let { requireNotNull(it) { "Could not find resource file: $resourcePath" } }
                    .bufferedReader()
                    .use { it.readText() })

            /** Assumes `users.json` file exists in quark module `resources/sensitive` directory. */
            public fun fromDefaultResources(): Users =
                fromJavaResources(Users::class.java.classLoader, "sensitive/users.json")
        }
    }
}
