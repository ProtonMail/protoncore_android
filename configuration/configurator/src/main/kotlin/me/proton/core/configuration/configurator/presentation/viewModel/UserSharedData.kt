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

package me.proton.core.configuration.configurator.presentation.viewModel

import me.proton.core.configuration.configurator.quark.entity.User

object SharedData {
    var lastUsername: String = ""
    var lastPassword: String = ""
    var lastPlan: String = ""
    var lastUserId: Long = 0
    var usersList: List<User> = listOf()

    fun isNotEmpty() = lastUsername.isNotEmpty() && lastPassword.isNotEmpty()
    fun clean() {
        lastUsername = ""
        lastPassword = ""
        lastPlan = ""
        lastUserId = 0
    }

    fun setUser(username: String, password: String = "", id: Long = 0) {
        val user = usersList.find { it.name == username }
        if (user != null) {
            lastUsername = user.name
            lastUserId = user.id
        } else {
            lastUsername = username
            lastUserId = id
        }
        lastPlan = ""
        lastPassword = password
    }
}

fun getUser(): User {
    return User(
        id = SharedData.lastUserId,
        name = SharedData.lastUsername,
        password = SharedData.lastPassword,
        plan = SharedData.lastPlan
    )
}