/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.test.android.libtests.auth

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import me.proton.android.core.coreexample.MainActivity
import me.proton.android.core.coreexample.MainInitializer
import me.proton.android.core.coreexample.di.ApplicationModule
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.test.MinimalSignUpExternalTests
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import me.proton.core.test.android.robot.CoreexampleRobot
import me.proton.core.test.rule.extension.protonActivityScenarioRule
import me.proton.test.fusion.Fusion.device
import org.junit.Before
import org.junit.Rule

@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
class SignupExternalTests : MinimalSignUpExternalTests {

    @get:Rule
    val protonRule = protonActivityScenarioRule<MainActivity>(
        afterHilt = {
            MainInitializer.init(it.targetContext)
        }
    )

    @BindValue
    val appStore: AppStore = AppStore.GooglePlay

    @BindValue
    val product: Product = Product.Vpn

    @BindValue
    val requiredAccountType: AccountType = AccountType.External

    /**
     * We need this override since in real Proton client we trigger sign up from Login screen
     * and in coreexample app this is done by clicking on "Signup External" button.
     */
    @Before
    override fun goToExternalSignup() {
        device.pressBack()
        CoreexampleRobot().signupExternal()
    }
}
