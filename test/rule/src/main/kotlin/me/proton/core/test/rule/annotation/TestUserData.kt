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

package me.proton.core.test.rule.annotation

import me.proton.core.test.rule.extension.seedTestUserData
import me.proton.core.util.kotlin.EMPTY_STRING
import me.proton.core.util.kotlin.random

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Suppress("LongParameterList")
public annotation class TestUserData(
    val name: String = EMPTY_STRING,
    val password: String = "password",
    val recoveryEmail: String = EMPTY_STRING,
    val status: Status = Status.Active,
    val authVersion: Int = 4,
    val createAddress: Boolean = true,
    val genKeys: GenKeys = GenKeys.Curve25519,
    val mailboxPassword: String = EMPTY_STRING,
    val external: Boolean = false,
    val toTpSecret: String = EMPTY_STRING,
    val recoveryPhone: String = EMPTY_STRING,
    val externalEmail: String = EMPTY_STRING,
    val vpnSettings: String = EMPTY_STRING,
    val creationTime: String = EMPTY_STRING,

    val shouldSeed: Boolean = true,
) {
    public enum class Status {
        Deleted, Disabled, Active, VPNAdmin, Admin, Super
    }

    public enum class GenKeys {
        None, RSA2048, RSA4096, Curve25519
    }

    public companion object {
        public fun randomUsername(): String =
            arrayOf(('A'..'Z').toList(), ('1'..'9').toList())
                .map { String.random(length = 4, charPool = it) }
                .let { "proton${it[0]}${it[1]}" }

        public val withRandomUsername: TestUserData get() = TestUserData(randomUsername())
    }
}

public fun TestUserData.handleExternal(
    username: String = TestUserData.randomUsername()
): TestUserData = TestUserData(
    name = if (shouldHandleExternal) username else name,
    password,
    recoveryEmail,
    status,
    authVersion,
    createAddress,
    genKeys,
    mailboxPassword,
    external,
    toTpSecret,
    recoveryPhone,
    externalEmail = if (shouldHandleExternal) "$username@example.lt" else externalEmail
)

public val TestUserData.annotationTestData: AnnotationTestData<TestUserData>
    get() = AnnotationTestData(
        default = this,
        implementation = { data ->
            seedTestUserData(data)
        }
    )

public val TestUserData.shouldHandleExternal: Boolean
    get() = externalEmail.isEmpty() && external
