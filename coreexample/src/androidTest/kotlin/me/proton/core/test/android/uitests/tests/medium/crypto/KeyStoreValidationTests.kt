/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.test.android.uitests.tests.medium.crypto

import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.espresso.intent.Intents
import me.proton.core.crypto.validator.R
import me.proton.core.crypto.validator.presentation.ui.CryptoValidatorErrorDialogActivity
import me.proton.core.test.android.instrumented.ProtonTest
import me.proton.core.test.android.instrumented.rules.RetryRule
import me.proton.core.test.android.instrumented.utils.Shell
import me.proton.core.test.android.instrumented.utils.waitUntil
import me.proton.core.test.android.robots.other.KeyStoreErrorRobot
import me.proton.core.test.android.uitests.tests.BaseTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class KeyStoreValidationTests {

    class TestExecutionWatcher : TestWatcher() {
        override fun failed(e: Throwable?, description: Description?) = Shell.saveToFile(description)
    }

    private val testWatcher = TestExecutionWatcher()
    private val retryRule = RetryRule(CryptoValidatorErrorDialogActivity::class.java, 2)

    @Rule
    @JvmField
    val ruleChain = RuleChain
        .outerRule(ProtonTest.testName)
        .around(testWatcher)
        .around(retryRule)!!

    private val robot = KeyStoreErrorRobot()

    @Before
    fun setup() {
        BaseTest.authHelper.logoutAll()
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun showsExitButtonIfThereAreNoAccounts() {
        // GIVEN
        val scenario = launchActivity<CryptoValidatorErrorDialogActivity>()
        // THEN
        robot.verify {
            dialogIsDisplayed()
            exitButtonIsDisplayed()
        }
        scenario.close()
    }

    @Test
    fun showsLogoutButtonIfThereAreAccounts() {
        // GIVEN
        val user = BaseTest.users.getUser()
        BaseTest.authHelper.login(user.name, user.password)
        val scenario = launchActivity<CryptoValidatorErrorDialogActivity>()
        // THEN
        robot.verify {
            dialogIsDisplayed()
            logoutButtonIsDisplayed()
        }
        scenario.close()
    }

    @Test
    fun logoutButtonRemovesAllAccountsAndClosesTheDialog() {
        // GIVEN
        val user = BaseTest.users.getUser()
        BaseTest.authHelper.login(user.name, user.password)
        val scenario = launchActivity<CryptoValidatorErrorDialogActivity>()
        robot.verify { dialogIsDisplayed() }
        // WHEN
        robot.tapLogout()
        // THEN
        waitUntil { !BaseTest.authHelper.hasAccounts() }
        scenario.assertIsDestroyed()
    }

    @Test
    fun continueClosesTheDialog() {
        // GIVEN
        val scenario = launchActivity<CryptoValidatorErrorDialogActivity>()
        robot.verify { dialogIsDisplayed() }
        // WHEN
        robot.tapContinue()
        // THEN
        scenario.assertIsDestroyed()
    }

    @Test
    fun moreInfoButtonOpensBrowser() {
        // GIVEN
        val scenario = launchActivity<CryptoValidatorErrorDialogActivity>()
        robot.verify { dialogIsDisplayed() }
        // WHEN
        robot.tapMoreInfo()
        // THEN
        val url = ProtonTest.getTargetContext().getString(R.string.crypto_keystore_help_url)
        robot.intent.hasDataUri(Uri.parse(url))
        scenario.close()
    }
}

private fun ActivityScenario<*>.assertIsDestroyed() = waitUntil { state == Lifecycle.State.DESTROYED }
