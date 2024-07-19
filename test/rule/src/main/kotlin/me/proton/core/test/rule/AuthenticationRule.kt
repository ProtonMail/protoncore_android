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
import kotlinx.coroutines.runBlocking
import me.proton.core.auth.domain.testing.LoginTestHelper
import me.proton.core.auth.presentation.testing.ProtonTestEntryPoint
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
    private val protonRule: ProtonRule
) : TestWatcher() {

    override fun starting(description: Description) {
        runBlocking {
            printInfo("Starting ${AuthenticationRule::class.java.simpleName}")
            if (protonRule.userConfig!!.logoutBefore) {
                logout()
            }

            protonRule.testDataRule.mainTestUser?.let { mainTestUser ->
                // Authenticate only if loginBefore is true
                if (mainTestUser.loginBefore) {
                    val loginIdentifier = when {
                        mainTestUser.name.isNotEmpty() -> mainTestUser.name
                        mainTestUser.email.isNotEmpty() -> mainTestUser.email
                        else -> throw IllegalStateException("User authentication was requested but both email and name fields are empty.")
                    }

                    loginIdentifier.let {
                        printInfo("Logging in: $it / ${mainTestUser.password} ...")
                        val loginTime = measureTime {
                            authHelper.login(it, mainTestUser.password)
                        }
                        printInfo("Logged in ${loginTime.inWholeSeconds} seconds.")
                    }
                }
            } ?: printInfo("No users required to login before test execution. Skipping authentication.")

            printInfo("Done with starting ${this::class.java.simpleName}")
        }
    }

    override fun finished(description: Description) {
        if (protonRule.userConfig!!.logoutAfter) {
            logout()
        }
    }

    private fun logout() {
        printInfo("Logging out all users")
        runCatching { authHelper.logoutAll() }
    }

    public companion object {

        private val protonTestEntryPoint by lazy {
            EntryPointAccessors.fromApplication(
                ApplicationProvider.getApplicationContext<Application>(),
                ProtonTestEntryPoint::class.java
            )
        }

        public val authHelper: LoginTestHelper by lazy { protonTestEntryPoint.loginTestHelper }
    }
}
