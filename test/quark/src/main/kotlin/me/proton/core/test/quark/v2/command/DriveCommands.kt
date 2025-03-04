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

public fun QuarkCommand.populate(
    user: User,
    scenario: Int = 0,
    isDevice: Boolean = false,
    isPhotos: Boolean = false,
    isAnonymous: Boolean = false,
    sharingUser: User? = null
): Response =
    route("quark/drive:populate")
        .args(
            listOfNotNull(
                "-u" to user.name,
                "-p" to user.password,
                "-S" to scenario.toString(),
                isDevice.optionalArg("-d"),
                isPhotos.optionalArg("--photo"),
                isAnonymous.optionalArg("--anonymous"),
                sharingUser?.name?.optionalArg("--sharing-username"),
                sharingUser?.password?.optionalArg("--sharing-user-pass"),
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

public fun QuarkCommand.populate(
    userName: String,
    password: String,
    scenario: Int = 0,
    isDevice: Boolean = false,
    isPhotos: Boolean = false,
    isAnonymous: Boolean = false,
    sharingUserName: String? = null,
    sharingUserPassword: String? = null
): Response =
    route("quark/drive:populate")
        .args(
            listOfNotNull(
                "-u" to userName,
                "-p" to password,
                "-S" to scenario.toString(),
                isDevice.optionalArg("-d"),
                isPhotos.optionalArg("--photo"),
                isAnonymous.optionalArg("--anonymous"),
                sharingUserName?.optionalArg("--sharing-username"),
                sharingUserPassword?.optionalArg("--sharing-user-pass"),
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

private fun Boolean.optionalArg(
    name: String,
): Pair<String, String>? = takeIf { it }?.let { name to it.toString() }

private fun String.optionalArg(
    name: String,
): Pair<String, String>? = takeIf { it.isNotEmpty() }?.let { name to it }

public fun QuarkCommand.quotaSetUsedSpace(
    user: User,
    usedSpace: String,
    product: String,
): Response =
    route("quark/drive:quota:set-used-space")
        .args(
            listOf(
                "--user-id" to user.decryptedUserId.toString(),
                "--used-space" to usedSpace,
                "--product" to product
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

public fun QuarkCommand.quotaSetUsedSpace(
    userId: Long,
    usedSpace: String,
    product: String,
): Response =
    route("quark/drive:quota:set-used-space")
        .args(
            listOf(
                "--user-id" to userId.toString(),
                "--used-space" to usedSpace,
                "--product" to product
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

public fun QuarkCommand.mailQuotaSetUsedSpace(
    userId: Long,
    usedSpace: String,
    product: String,
): Response =
    route("quark/account:quota:set-used-space")
        .args(
            listOf(
                "--user-id" to userId.toString(),
                "--used-space" to usedSpace,
                "--product" to product
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

public fun QuarkCommand.volumeCreate(
    user: User
): Response = volumeCreate(
    user.decryptedUserId.toString(),
    user.name,
    user.password,
    user.addressID
)

public fun QuarkCommand.volumeCreate(
    uid: String,
    username: String,
    pass: String,
    addressId: String = ""
): Response =
    route("quark/drive:volume:create")
        .args(
            listOf(
                "--uid" to uid,
                "--username" to username,
                "--pass" to pass,
                "--address-id" to addressId
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

