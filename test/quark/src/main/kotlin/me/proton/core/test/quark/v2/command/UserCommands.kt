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

import me.proton.core.test.quark.Quark.GenKeys
import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.response.CreateUserAddressQuarkResponse
import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.test.quark.response.FixtureDoctrineLoadResponse
import me.proton.core.test.quark.response.FixtureLoadResponse
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.executeHttpURLConnectionRequest
import me.proton.core.test.quark.v2.toEncodedArgs
import okhttp3.Response
import org.json.JSONObject

import java.net.URL

public const val USERS_CREATE: String = "quark/raw::user:create"
public const val FIXTURE_LOAD: String = "quark/raw::qa:fixtures:load"
public const val DOCTRINE_FIXTURE_LOAD: String = "quark/raw::doctrine:fixtures:load"
public const val USERS_CREATE_ADDRESS: String = "quark/raw::user:create:address"
public const val USERS_EXPIRE_SESSIONS: String = "quark/raw::user:expire:sessions"
public const val USERS_RESET: String = "quark/raw::user:reset"
public const val USERS_REACTIVATE: String = "quark/raw::user:reactivate"
public const val USERS_DELETE: String = "quark/raw::user:delete"

public fun QuarkCommand.userCreate(
    user: User = User(),
    createAddress: CreateAddress? = CreateAddress.WithKey(GenKeys.Curve25519)
): CreateUserQuarkResponse {
    val args = listOf(
        "--external" to if (user.isExternal) "true" else "",
        "--external-email" to if (user.isExternal) user.email else "",
        "-N" to user.name,
        "-p" to user.password,
        "-m" to user.passphrase,
        "-r" to user.recoveryEmail,
        "-c" to if (createAddress is CreateAddress.NoKey) "true" else "",
        "-k" to if (createAddress is CreateAddress.WithKey) createAddress.genKeys.name else "",
        "--format" to "json"
    ).toEncodedArgs(ignoreEmpty = true)

    val response =
        route(USERS_CREATE)
            .args(args)
            .build()
            .let {
                client.executeQuarkRequest(it)
            }

    return json.decodeFromString(response.body!!.string())
}

public fun QuarkCommand.userCreateAddress(
    decryptedUserId: Long,
    password: String,
    email: String,
    genKeys: GenKeys = GenKeys.Curve25519,
    isPrimary: Boolean = false
): CreateUserAddressQuarkResponse =
    route(USERS_CREATE_ADDRESS)
        .args(
            listOf(
                "userID" to decryptedUserId.toString(),
                "password" to password,
                "email" to email,
                "--gen-keys" to genKeys.name,
                "--primary" to "$isPrimary",
                "--format" to "json"
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }
        .let {
            json.decodeFromString(it.body!!.string())
        }


public fun QuarkCommand.expireSession(username: String, expireRefreshToken: Boolean = false): Response =
    route(USERS_EXPIRE_SESSIONS)
        .args(
            listOf(
                "User" to username,
                "--refresh" to if (expireRefreshToken) "null" else ""
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

public fun QuarkCommand.userDelete(id: Int): Response =
    route(USERS_DELETE)
        .args(
            listOf(
                "-u" to id.toString(),
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

public fun QuarkCommand.userReset(id: String): Response =
    route(USERS_DELETE)
        .args(
            listOf(
                "-u" to id,
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }

public fun QuarkCommand.loadFixture(scenario: String, nexusUrl: String): FixtureLoadResponse {
    val args = listOf(
        "definition-paths[]=nexus://Mail/ios/ios.${scenario}",
        "--source[]=nexus:nexus:$nexusUrl?repository=TestData",
        "--output-format=json"
    ).toTypedArray()

    val response =
        route(FIXTURE_LOAD)
            .args(args)
            .build()
            .let {
                client.executeQuarkRequest(it)
            }
    return json.decodeFromString(response.body!!.string())
}

public fun QuarkCommand.loadDoctrineFixture(scenario: String): FixtureDoctrineLoadResponse {
    val queryParams = listOf(
        "--append=1",
        "--group[]=$scenario"
    ).joinToString("&")

    val url = URL("$baseUrl/$DOCTRINE_FIXTURE_LOAD?$queryParams")

    val response = executeHttpURLConnectionRequest(url)
    val convertedResponse = convertQuarkFixtureLoadResponseToJson(response)
    return convertedResponse!!.let { json.decodeFromString(convertedResponse) }
}

public sealed class CreateAddress {
    public data object NoKey : CreateAddress()
    public data class WithKey(val genKeys: GenKeys = GenKeys.Curve25519) : CreateAddress()
}

public fun convertQuarkFixtureLoadResponseToJson(inputString: String): String? {
    val lines = inputString.split("\n").map { it.trim() }.filter { it.isNotEmpty() }

    val jsonObject = mutableMapOf<String, String>()

    for (line in lines) {
        val components = line.split(":", limit = 2)
        if (components.size == 2) {
            val key = components[0].trim()
            val value = components[1].trim()
            jsonObject[key] = value
        }
    }

    return try {
        JSONObject(jsonObject as Map<String, Any>).toString(4)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
