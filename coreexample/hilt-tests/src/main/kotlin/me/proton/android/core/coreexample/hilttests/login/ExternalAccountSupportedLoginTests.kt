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

package me.proton.android.core.coreexample.hilttests.login

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.proton.android.core.coreexample.Constants
import me.proton.android.core.coreexample.MainActivity
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.android.core.coreexample.di.ApplicationModule
import me.proton.android.core.coreexample.hilttests.di.DriveApiClient
import me.proton.android.core.coreexample.hilttests.usecase.PerformUiLogin
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import me.proton.core.key.domain.entity.key.KeyFlags
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.test.android.instrumented.ProtonTest
import me.proton.core.test.quark.Quark
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.AddressType
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import org.junit.Rule
import javax.inject.Inject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import me.proton.core.test.quark.data.User as TestUser

@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
class ExternalAccountSupportedLoginTests : ProtonTest(MainActivity::class.java, tries = 1) {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    val apiClient: CoreExampleApiClient = DriveApiClient

    @BindValue
    val appStore: AppStore = AppStore.GooglePlay

    @BindValue
    val product: Product = Product.Drive

    @BindValue
    val accountType: AccountType = AccountType.External

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var performUiLogin: PerformUiLogin

    @BeforeTest
    fun prepare() {
        hiltRule.inject()
        extraHeaderProvider.addHeaders("X-Accept-ExtAcc" to "true")
    }

    @Test
    fun loginWithExternalAccountNoKeys() {
        val testUser = TestUser(
            name = "",
            email = "${TestUser.randomUsername()}@externaldomain.test",
            isExternal = true
        )
        quark.userCreate(testUser, Quark.CreateAddress.NoKey)

        performUiLogin(testUser.email, testUser.password)

        val account = runBlocking { accountManager.getPrimaryAccount().first()!! }
        val addresses = runBlocking { userManager.getAddresses(account.userId) }
        val user = runBlocking { userManager.getUser(account.userId) }

        assertEquals(testUser.email, account.email)
        assertEquals(testUser.email, user.email)
        verifyKeys(testUser.email, addresses, user)
    }

    @Test
    fun loginWithExternalAccountNoKeysTwoPassMode() {
        val mailboxPass = "mailbox-password"
        val testUser = TestUser(
            name = TestUser.randomUsername(),
            isExternal = false,
            passphrase = mailboxPass
        )
        val email = "${testUser.name}@externaldomain.test"
        val (_, createUserResponse) = quark.userCreate(
            testUser,
            createAddress = Quark.CreateAddress.WithKey()
        )

        quark.userCreateAddress(
            decryptedUserId = createUserResponse.decryptedUserId,
            password = testUser.password,
            email = email,
            genKeys = Quark.GenKeys.None
        )

        performUiLogin(email, testUser.password, mailboxPass = mailboxPass)

        val account = runBlocking { accountManager.getPrimaryAccount().first()!! }
        val addresses = runBlocking { userManager.getAddresses(account.userId) }
        val user = runBlocking { userManager.getUser(account.userId) }

        assertEquals(email, account.email)
        assertEquals(createUserResponse.email, user.email)
        verifyKeys(email, addresses, user)
    }

    @Test
    fun loginWithExternalAccountWithUserKeyNoAddressKey() {
        // Setup: create a user (not external user) with an internal address.
        // Then add an external address, but without address key (`GenKeysOption.None`).

        val username = TestUser.randomUsername()
        val email = "$username@externaldomain.test"
        val testUser = TestUser(
            name = username,
            isExternal = false
        )
        val (_, createUserResponse) = quark.userCreate(
            testUser,
            createAddress = Quark.CreateAddress.WithKey()
        )

        quark.userCreateAddress(
            decryptedUserId = createUserResponse.decryptedUserId,
            password = testUser.password,
            email = email,
            genKeys = Quark.GenKeys.None
        )

        performUiLogin(email, testUser.password)

        val account = runBlocking { accountManager.getPrimaryAccount().first()!! }
        val addresses = runBlocking { userManager.getAddresses(account.userId) }
        val user = runBlocking { userManager.getUser(account.userId) }

        assertEquals(email, account.email)
        assertEquals(createUserResponse.email, user.email)

        verifyKeys(email, addresses, user)
    }

    @Test
    fun loginWithExternalAccountWithMultipleExternalAddressesButNoAddressKeys() {
        // Setup: create a user (not external user) with an internal address.
        // Then add an two external addresses, but without address keys (`GenKeysOption.None`).

        val username1 = TestUser.randomUsername()
        val username2 = TestUser.randomUsername()
        val email1 = "$username1@externaldomain.test"
        val email2 = "$username2@externaldomain.test"
        val testUser = TestUser(
            name = TestUser.randomUsername(),
            isExternal = false
        )
        val (_, createUserResponse) = quark.userCreate(
            testUser,
            createAddress = Quark.CreateAddress.WithKey()
        )

        quark.userCreateAddress(
            decryptedUserId = createUserResponse.decryptedUserId,
            password = testUser.password,
            email = email1,
            genKeys = Quark.GenKeys.None
        )
        quark.userCreateAddress(
            decryptedUserId = createUserResponse.decryptedUserId,
            password = testUser.password,
            email = email2,
            genKeys = Quark.GenKeys.None
        )

        performUiLogin(email2, testUser.password)

        val account = runBlocking { accountManager.getPrimaryAccount().first()!! }
        val addresses = runBlocking { userManager.getAddresses(account.userId) }
        val user = runBlocking { userManager.getUser(account.userId) }

        assertEquals(email2, account.email)
        assertEquals(createUserResponse.email, user.email)
        verifyKeys(email1, addresses, user)
        verifyKeys(email2, addresses, user)
    }

    @Test
    fun loginWithEAWithUserKeyAndAddressKey() {
        // Setup: create a user (not external user) with an internal address.
        // Then add an external address, with address key (`GenKeysOption.Curve25519`).

        val username = TestUser.randomUsername()
        val email = "$username@externaldomain.test"
        val testUser = TestUser(
            name = username,
            isExternal = false
        )
        val (_, createUserResponse) = quark.userCreate(
            testUser,
            createAddress = Quark.CreateAddress.WithKey()
        )

        quark.userCreateAddress(
            decryptedUserId = createUserResponse.decryptedUserId,
            password = testUser.password,
            email = email,
            genKeys = Quark.GenKeys.Curve25519
        )

        performUiLogin(email, testUser.password)

        val account = runBlocking { accountManager.getPrimaryAccount().first()!! }
        val addresses = runBlocking { userManager.getAddresses(account.userId) }
        val user = runBlocking { userManager.getUser(account.userId) }

        assertEquals(email, account.email)
        assertEquals(createUserResponse.email, user.email)

        verifyKeys(email, addresses, user)
    }

    private fun verifyKeys(expectedEmail: String, addresses: List<UserAddress>, user: User) {
        assertTrue(user.keys.isNotEmpty(), "User keys are missing.")

        val address = addresses.first { it.email == expectedEmail && it.type == AddressType.External }
        assertNotNull(address, "Could not find external address, got: $addresses")

        val addressKeys = address.keys
        assertTrue(addressKeys.isNotEmpty(), "User address keys are missing.")
        assertTrue(addressKeys.all { it.active }, "Some address keys are not active (address=$address).")
        assertTrue(
            addressKeys.all { it.flags and (KeyFlags.EmailNoEncrypt or KeyFlags.EmailNoSign) != 0 },
            "Some address keys have incorrect flags (address=$address)."
        )
    }

    companion object {
        private val quark = Quark.fromDefaultResources(Constants.QUARK_HOST, Constants.PROXY_TOKEN)
    }
}
