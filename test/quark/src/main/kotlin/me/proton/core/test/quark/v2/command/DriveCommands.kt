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

package me.proton.core.test.quark.v2.command

import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.toEncodedArgs
import okhttp3.Response

public const val DRIVE_POPULATE_USER_WITH_DATA: String = "quark/raw::drive:populate"

public fun QuarkCommand.populateUserWithData(
    user: User,
    hasPhotos: Boolean = false,
    withDevice: Boolean = false
): Response {
    val args = mutableListOf(
        "-u" to user.name,
        "-p" to user.password,
        "-S" to user.dataSetScenario
    )

    if (hasPhotos) {
        args.add("--photo" to "$hasPhotos")
    }

    if (withDevice) {
        args.add("--device" to "$withDevice")
    }

    return route(DRIVE_POPULATE_USER_WITH_DATA)
        .args(args.toEncodedArgs())
        .build()
        .let { client.executeQuarkRequest(it) }
}