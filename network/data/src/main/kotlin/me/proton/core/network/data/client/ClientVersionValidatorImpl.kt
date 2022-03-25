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

package me.proton.core.network.data.client

import me.proton.core.network.domain.client.ClientVersionValidator

class ClientVersionValidatorImpl : ClientVersionValidator {

    override fun validate(versionName: String?): Boolean {
        val components = versionName?.split("@").orEmpty()
        if (components.count() != 2) return false
        val (name, version) = components
        return isValidName(name) && isValidVersion(version)
    }

    private fun isValidName(name: String) =
        Regex("^[a-z_]+-[a-z_]+(?:-[a-z_]+)?\$").matches(name)

    private fun isValidVersion(version: String) =
        Regex("^\\d+?\\.\\d+?\\.\\d+?(-((stable|RC|beta|alpha)(\\.\\d+)?|dev)|)?(\\+[0-9A-Za-z\\-]+)?\$")
            .matches(version)

}
