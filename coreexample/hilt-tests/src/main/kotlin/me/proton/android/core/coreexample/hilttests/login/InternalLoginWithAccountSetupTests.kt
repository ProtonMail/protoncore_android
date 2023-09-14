/*
 * Copyright (c) 2022 Proton AG
 *
 * This file is part of Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see https://www.gnu.org/licenses/.
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
import me.proton.android.core.coreexample.hilttests.di.MailApiClient
import me.proton.android.core.coreexample.hilttests.usecase.PerformUiLogin
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.test.android.instrumented.ProtonTest
import me.proton.core.test.quark.Quark
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.canEncrypt
import me.proton.core.user.domain.extension.canVerify
import me.proton.core.user.domain.repository.DomainRepository
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.proton.core.test.quark.data.User as TestUser

@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
class InternalLoginWithAccountSetupTests : ProtonTest(MainActivity::class.java, tries = 1) {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    val apiClient: CoreExampleApiClient = MailApiClient

    @BindValue
    val appStore: AppStore = AppStore.GooglePlay

    @BindValue
    val product: Product = Product.Mail

    @BindValue
    val accountType: AccountType = AccountType.Internal

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var domainRepository: DomainRepository

    @Inject
    lateinit var performUiLogin: PerformUiLogin

    @Inject
    lateinit var userManager: UserManager

    @BeforeTest
    fun prepare() {
        hiltRule.inject()
    }

    @Test
    fun userWithNoKeys() {
        val (testUser, response) = quark.userCreate(createAddress = null)
        performUiLogin(testUser.name, testUser.password)

        verifyAccountSetup(expectedAddressCount = 1)
        verifyUnlockUser(testUser.password, response.userId, UserManager.UnlockResult.Success)
    }

    @Test
    fun userWithPassphraseAndKeys() {
        val mailboxPass = "test-passphrase"
        val testUser = TestUser(passphrase = mailboxPass)
        val (_, response) = quark.userCreate(testUser, Quark.CreateAddress.WithKey())

        performUiLogin(testUser.name, testUser.password, mailboxPass = mailboxPass)

        verifyAccountSetup(expectedAddressCount = 1)
        verifyUnlockUser(mailboxPass, response.userId, UserManager.UnlockResult.Success)
        verifyUnlockUser(testUser.password, response.userId, UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase)
    }

    @Test
    fun userWithPassphraseAndMissingAddressKey() {
        val mailboxPass = "test-passphrase"
        val testUser = TestUser(passphrase = mailboxPass)
        val (_, response) = quark.userCreate(
            testUser,
            Quark.CreateAddress.WithKey()
        )
        val domains = runBlocking { domainRepository.getAvailableDomains(null) }
        val email = "${response.name}_2@${domains.first()}"

        quark.userCreateAddress(response.decryptedUserId, mailboxPass, email, Quark.GenKeys.None)

        performUiLogin(testUser.name, testUser.password, mailboxPass = mailboxPass)

        verifyAccountSetup(expectedAddressCount = 2)
        verifyUnlockUser(testUser.password, response.userId, UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase)
        verifyUnlockUser(mailboxPass, response.userId, UserManager.UnlockResult.Success)
    }

    private fun verifyAccountSetup(expectedAddressCount: Int) {
        val account = runBlocking { accountManager.getPrimaryAccount().first()!! }
        val addresses = runBlocking { userManager.getAddresses(account.userId) }
        val user = runBlocking { userManager.getUser(account.userId) }

        assertTrue(user.keys.isNotEmpty(), "User keys are missing.")
        user.keys.forEach { userKey ->
            assertTrue(userKey.active == true, "One of user keys is not active.")
        }

        assertEquals(expectedAddressCount, addresses.size)
        addresses.forEach { userAddress ->
            assertTrue(userAddress.canSend, "One of user addresses cannot send.")
            assertTrue(userAddress.canReceive, "One of user addresses cannot receive.")
            assertTrue(userAddress.enabled, "One of user addresses is not enabled.")

            assertTrue(userAddress.keys.isNotEmpty())
            userAddress.keys.forEach { key ->
                assertTrue(key.active, "One of user address keys is not active.")
                assertTrue(key.canEncrypt(), "One of user address keys cannot encrypt.")
                assertTrue(key.canVerify(), "One of user address keys cannot verify.")
            }
            assertTrue(
                userAddress.keys.any { it.privateKey.isPrimary },
                "At least one user address key should be marked as primary key."
            )
        }
    }

    /** Verify the [expected] result of unlocking a user with a given [pass].
     * @param pass User's password or mailbox passphrase.
     * @param userId Id of the user which will be unlocked.
     * @param expected Expected result of unlocking.
     */
    private fun verifyUnlockUser(pass: String, userId: String, expected: UserManager.UnlockResult) {
        assertEquals(
            expected,
            runBlocking {
                userManager.unlockWithPassword(UserId(userId), PlainByteArray(pass.toByteArray()))
            }
        )
    }

    companion object {
        private val quark = Quark.fromDefaultResources(Constants.QUARK_HOST, Constants.PROXY_TOKEN)
    }
}
