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

package me.proton.android.core.coreexample.hilttests

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import me.proton.android.core.coreexample.BuildConfig
import me.proton.android.core.coreexample.Constants
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.android.core.coreexample.di.ApplicationModule
import me.proton.android.core.coreexample.hilttests.mocks.AndroidTestApiClient
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.ClientSecret
import me.proton.core.auth.test.ExternalAccountSignupTests
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.test.quark.Quark
import org.junit.Rule
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
class DriveExternalAccountSignupTests : ExternalAccountSignupTests() {

    override val quark = Quark.fromDefaultResources(Constants.QUARK_HOST, BuildConfig.PROXY_TOKEN)

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    override lateinit var extraHeaderProvider: ExtraHeaderProvider

    @BindValue
    val apiClient: CoreExampleApiClient = AndroidTestApiClient(
        appName = "android-drive",
        productName = "ProtonDrive",
        versionName = "1.0.0"
    )

    @BindValue
    val appStore: AppStore = AppStore.GooglePlay

    @BindValue
    val product: Product = Product.Drive

    @BindValue
    val accountType: AccountType = AccountType.External

    @BindValue
    @ClientSecret
    val clientSecret: String = ""

    override fun setUp() {
        hiltRule.inject()
        super.setUp()
    }
}
