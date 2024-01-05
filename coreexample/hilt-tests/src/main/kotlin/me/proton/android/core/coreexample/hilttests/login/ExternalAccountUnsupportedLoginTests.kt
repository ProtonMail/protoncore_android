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
import me.proton.android.core.coreexample.MainActivity
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.android.core.coreexample.di.ApplicationModule
import me.proton.android.core.coreexample.hilttests.di.MailApiClient
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.test.android.instrumented.ProtonTest
import me.proton.core.test.android.robots.auth.AddAccountRobot
import me.proton.core.test.quark.data.User
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.command.CreateAddress
import me.proton.core.test.quark.v2.command.userCreate
import org.junit.Rule
import javax.inject.Inject
import kotlin.test.BeforeTest
import kotlin.test.Test

@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
class ExternalAccountUnsupportedLoginTests : ProtonTest(MainActivity::class.java, tries = 1) {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    @Inject
    lateinit var quark: QuarkCommand

    @BindValue
    val apiClient: CoreExampleApiClient = MailApiClient.copy(
        versionName = "3.0.13" // MinExternalAccountsVersion is '>=3.0.14'
    )

    @BindValue
    val appStore: AppStore = AppStore.GooglePlay

    @BindValue
    val product: Product = Product.Mail

    @BindValue
    val accountType: AccountType = AccountType.Internal

    private lateinit var user: User

    @BeforeTest
    fun prepare() {
        user = User(
            name = "",
            email = "${User.randomUsername()}@proton.wtf",
            isExternal = true
        )
        hiltRule.inject()
    }

    @Test
    fun cannotLoginWithExternalAccountOnOldApps() {
        quark.userCreate(user, CreateAddress.NoKey)

        AddAccountRobot()
            .signIn()
            .username(user.email)
            .password(user.password)
            .signIn<AddAccountRobot>()
            .verify {
                // Note: server may return different error,
                // depending on `BLOCK_EXTERNAL_SIGNIN` env variable.
                appVersionTooOldForExternalAccounts()
            }
    }
}
