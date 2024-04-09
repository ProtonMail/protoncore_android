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

package me.proton.core.test.rule

import android.annotation.SuppressLint
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.EntryPointAccessors
import me.proton.core.auth.domain.testing.LoginTestHelper
import me.proton.core.auth.presentation.testing.ProtonTestEntryPoint
import me.proton.core.test.rule.entity.UserConfig
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import kotlin.time.measureTime

/**
 * A JUnit test rule for managing user authentication states before and after tests.
 * It utilizes [LoginTestHelper] for performing login and logout operations
 * based on the provided [ProtonRule.UserConfig].
 */
@SuppressLint("RestrictedApi")
public class AuthenticationRule(
    config: () -> UserConfig,
) : TestWatcher() {

    private val userConfig by lazy(config)

    private val loginTestHelper: LoginTestHelper by lazy {
        protonTestEntryPoint.loginTestHelper
    }

    private val protonTestEntryPoint by lazy {
        EntryPointAccessors.fromApplication(
            ApplicationProvider.getApplicationContext<Application>(),
            ProtonTestEntryPoint::class.java
        )
    }

    override fun starting(description: Description) {
        if (userConfig.logoutBefore) {
            logout()
        }

        userConfig.userData?.let {
            if (userConfig.loginBefore) {
                printInfo("Logging in: ${it.name} / ${it.password} ...")

                val loginTime = measureTime {
                    loginTestHelper.login(it.name, it.password)
                }

                printInfo("Logged in in ${loginTime.inWholeSeconds} seconds.")
            }
        }
    }

    override fun finished(description: Description) {
        if (userConfig.logoutAfter) {
            logout()
        }
    }

    private fun logout() {
        printInfo("Logging out all users")
        runCatching { loginTestHelper.logoutAll() }
    }
}
