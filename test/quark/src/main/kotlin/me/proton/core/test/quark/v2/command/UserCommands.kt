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

import kotlinx.serialization.decodeFromString
import me.proton.core.test.quark.Quark.GenKeys
import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.response.CreateUserAddressQuarkResponse
import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.QuarkCommand.Route
import okhttp3.Response
import java.net.URLEncoder

public fun QuarkCommand.userCreate(
    user: User = User(),
    createAddress: CreateAddress? = CreateAddress.WithKey(GenKeys.Curve25519)
): CreateUserQuarkResponse {
    val args = arrayOf(
        if (user.isExternal) "-e=true" else "",
        if (user.isExternal) "-em=${URLEncoder.encode(user.email, "UTF-8")}" else "",
        if (user.name.isNotEmpty()) "-N=${user.name}" else "",
        if (user.password.isNotEmpty()) "-p=${user.password}" else "",
        if (user.passphrase.isNotEmpty()) "-m=${user.passphrase}" else "",
        if (user.recoveryEmail.isNotEmpty()) "-r=${user.recoveryEmail}" else "",
        if (createAddress is CreateAddress.NoKey) "-c=true" else "",
        if (createAddress is CreateAddress.WithKey) "-k=${createAddress.genKeys.name}" else "",
        "--format=json"
    )
    val response = route(Route.USERS_CREATE).args(args).build().execute().let {
        json.decodeFromString<CreateUserQuarkResponse>(it.body!!.string())
    }
    return response
}

public fun QuarkCommand.userCreateAddress(
    decryptedUserId: Long,
    password: String,
    email: String,
    genKeys: GenKeys = GenKeys.Curve25519
): CreateUserAddressQuarkResponse =
    route(Route.USERS_CREATE_ADDRESS)
        .args(
            listOf(
                "userID" to decryptedUserId.toString(),
                "password" to password,
                "email" to email,
                "--gen-keys" to genKeys.name,
                "--format" to "json"
            ).toEncodedArgs()
        )
        .build()
        .execute().let {
            json.decodeFromString(it.body!!.string())
        }


public fun QuarkCommand.expireSession(username: String, expireRefreshToken: Boolean = false): Response =
    route(Route.USERS_EXPIRE_SESSIONS)
        .args(
            arrayOf(
                "User=$username",
                if (expireRefreshToken) "--refresh=null" else ""
            )
        )
        .build()
        .execute()


public sealed class CreateAddress {
    public object NoKey : CreateAddress()
    public data class WithKey(val genKeys: GenKeys = GenKeys.Curve25519) : CreateAddress()
}
