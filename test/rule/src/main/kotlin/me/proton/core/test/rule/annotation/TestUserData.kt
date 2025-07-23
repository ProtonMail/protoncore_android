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

import me.proton.core.test.quark.data.Plan
import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.response.CreateUserQuarkResponse
import me.proton.core.test.rule.annotation.payments.TestSubscriptionData
import me.proton.core.util.kotlin.EMPTY_STRING
import me.proton.core.util.kotlin.random

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Suppress("LongParameterList")
public annotation class TestUserData(
    val authVersion: Int = 4,
    val createAddress: Boolean = true,
    val creationTime: String = EMPTY_STRING,
    val email: String = EMPTY_STRING,
    val isExternal: Boolean = false,
    val externalEmail: String = EMPTY_STRING,
    val genKeys: GenKeys = GenKeys.Curve25519,
    val passphrase: String = EMPTY_STRING,
    val name: String = EMPTY_STRING,
    val password: String = EMPTY_STRING,
    val recoveryEmail: String = EMPTY_STRING,
    val recoveryPhone: String = EMPTY_STRING,
    val status: Status = Status.Active,
    val twoFa: String = EMPTY_STRING,
    val vpnSettings: String = EMPTY_STRING,

    // Will be later added from CreateUserQuarkResponse.
    val id: String = EMPTY_STRING,
    val decryptedUserId: Long = 0L,
    val addressID: String = EMPTY_STRING,
    val decryptedAddressID: Long = 0L,

    val loginBefore: Boolean = false,
    val externalEmailMatchesExistingName: Boolean = false,

    // Can be later updated by TestSubscriptionData.
    val plan: Plan = Plan.ProtonFree,
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
                .map { String.random(length = 6, charPool = it) }
                .let { "proton${it[0]}${it[1]}" }

        public val withRandomUsername: TestUserData get() = TestUserData(name = randomUsername())
    }
}

/**
 * This function should be called before seeding to handle dynamically created data.
 */
public fun TestUserData.handleUserData(
    username: String = TestUserData.randomUsername()
): TestUserData = TestUserData(
    authVersion = authVersion,
    createAddress = createAddress,
    creationTime = creationTime,
    email = email,
    isExternal = isExternal,
    externalEmail = when {
        shouldHandleExternal -> "$username@example.lt"
        externalEmailMatchesExistingName -> "free@${username}.lt"
        else -> externalEmail
    },
    genKeys = genKeys,
    passphrase = passphrase,
    name = name.ifEmpty { username },
    password = password.ifEmpty { "password" },
    recoveryEmail = recoveryEmail.ifEmpty { "$username@example.lt" },
    recoveryPhone = recoveryPhone,
    status = status,
    twoFa = twoFa,
    vpnSettings = vpnSettings,
)

/**
 * This function should be called after seeding to update users with CreateUserQuarkResponse data.
 */
public fun TestUserData.copyWithQuarkResponseData(
    createdUserQuarkResponse: CreateUserQuarkResponse,
    loginBefore: Boolean
): TestUserData {
    return TestUserData(

        // From CreateUserQuarkResponse if not null.
        name = if (createdUserQuarkResponse.name != null) createdUserQuarkResponse.name!! else this.name,
        password = createdUserQuarkResponse.password,
        addressID = createdUserQuarkResponse.addressID ?: EMPTY_STRING,
        decryptedAddressID = createdUserQuarkResponse.decryptedAddressID ?: 0L,
        decryptedUserId = createdUserQuarkResponse.decryptedUserId,
        id = createdUserQuarkResponse.userId,
        recoveryEmail = createdUserQuarkResponse.recovery,
        recoveryPhone = createdUserQuarkResponse.recoveryPhone,
        authVersion = createdUserQuarkResponse.authVersion,
        email = if (createdUserQuarkResponse.email != null) createdUserQuarkResponse.email!! else this.email,

        // From TestUserData.
        externalEmail = this.externalEmail,
        isExternal = this.isExternal,
        twoFa = this.twoFa,

        // LoginBefore parameter from @SeedUser annotation.
        loginBefore = loginBefore
    )
}

/**
 * This function should be called after seeding to update users with CreateUserQuarkResponse data.
 */
public fun TestUserData.copyWithSubscriptionDetails(
    subscriptionData: TestSubscriptionData
): TestUserData {
    return TestUserData(

        // From CreateUserQuarkResponse if not null.
        name = this.name,
        password = this.password,
        addressID = this.addressID,
        decryptedAddressID = this.decryptedAddressID,
        decryptedUserId = this.decryptedUserId,
        id = this.id,
        recoveryEmail = this.recoveryEmail,
        recoveryPhone = this.recoveryPhone,
        authVersion = this.authVersion,
        email = this.email,

        // From TestUserData.
        externalEmail = this.externalEmail,
        isExternal = this.isExternal,
        twoFa = this.twoFa,

        // From TestSubscriptionData
        plan = subscriptionData.plan,

        // LoginBefore parameter from @SeedUser annotation.
        loginBefore = this.loginBefore
    )
}

public fun TestUserData.copyWithLoginFlag(
    loginBefore: Boolean
): TestUserData {
    return TestUserData(

        // From CreateUserQuarkResponse if not null.
        name = this.name,
        password = this.password,
        addressID = this.addressID,
        decryptedAddressID = this.decryptedAddressID,
        decryptedUserId = this.decryptedUserId,
        id = this.id,
        recoveryEmail = this.recoveryEmail,
        recoveryPhone = this.recoveryPhone,
        authVersion = this.authVersion,
        email = this.email,

        // From TestUserData.
        externalEmail = this.externalEmail,
        isExternal = this.isExternal,
        twoFa = this.twoFa,

        // From TestSubscriptionData
        plan = this.plan,

        // LoginBefore parameter from @PrepareUser annotation.
        loginBefore = loginBefore
    )
}

public fun TestUserData.mapToUser(): User = User(
    addressID = this.addressID,
    decryptedAddressID = this.decryptedAddressID,
    authVersion = this.authVersion,
    id = this.id,
    decryptedUserId = this.decryptedUserId,
    name = this. name,
    password = this.password,
    email = this.email,
    externalEmail = this.externalEmail,
    status = this.status.ordinal,
    passphrase = this.passphrase,
    twoFa = this.twoFa,
    phone = this.recoveryPhone,
    recoveryEmail = this.recoveryEmail,
    recoveryPhone = this.recoveryPhone,
    isExternal = this.isExternal,
    plan = this.plan,

    // Leaving default values. Can be updated later by creating similar extensions in clients code.
    country = EMPTY_STRING,
    cards = listOf(),
    paypal = EMPTY_STRING,
    dataSetScenario = EMPTY_STRING,
)

public val TestUserData.shouldHandleExternal: Boolean
    get() = externalEmail.isEmpty() && isExternal && !externalEmailMatchesExistingName
