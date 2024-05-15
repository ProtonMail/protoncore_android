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

package me.proton.core.userrecovery.test

import android.content.Context
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.proton.core.account.domain.entity.Account
import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.usecase.WaitForPrimaryAccount
import me.proton.core.test.quark.Quark
import me.proton.core.userrecovery.domain.repository.DeviceRecoveryRepository
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryHandler
import me.proton.core.userrecovery.presentation.compose.DeviceRecoveryNotificationSetup
import me.proton.test.fusion.Fusion.node
import me.proton.test.fusion.FusionConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import me.proton.core.userrecovery.presentation.R as UserRecoveryR

private const val DEFAULT_TIMEOUT_SEC = 30

public interface MinimalUserRecoveryTest {
    public val deviceRecoveryHandler: DeviceRecoveryHandler
    public val deviceRecoveryNotificationSetup: DeviceRecoveryNotificationSetup
    public val deviceRecoveryRepository: DeviceRecoveryRepository
    public val quark: Quark
    public val waitForPrimaryAccount: WaitForPrimaryAccount

    public fun signOut()

    public fun initFusion(composeTestRule: ComposeTestRule) {
        FusionConfig.Compose.testRule.set(composeTestRule)
    }

    @BeforeTest
    public fun prepare() {
        deviceRecoveryNotificationSetup.init()
        deviceRecoveryHandler.start()
    }

    @Test
    public fun verifyKeysAreRecovered() {
        val (user, response) = quark.userCreate()

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(user.name, user.password)

        val account = waitForPrimaryAccount()
        waitForRecoveryFileCreated(account)

        signOut()
        AddAccountRobot.uiElementsDisplayed(timeout = DEFAULT_TIMEOUT_SEC.seconds)

        val newPassword = "new-password"
        quark.resetPassword(
            userID = response.decryptedUserId,
            newPassword = newPassword
        )

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(user.name, newPassword)

        waitForPrimaryAccount()

        val notificationTitle = ApplicationProvider.getApplicationContext<Context>()
            .getString(UserRecoveryR.string.user_recovery_notification_title)

        // Open Notifications tray:
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.pressHome()
        uiDevice.openNotification()
        uiDevice.wait(
            Until.hasObject(By.textStartsWith(notificationTitle)),
            DEFAULT_TIMEOUT_SEC * 1000L
        )

        // Click the notification:
        uiDevice.findObject(By.textStartsWith(notificationTitle))?.click()
            ?: error("Could not find the notification for user recovery.")

        // Verify the alert dialog is shown
        node.withText(UserRecoveryR.string.user_recovery_dialog_title).await {
            assertIsDisplayed()
        }
    }

    private fun waitForRecoveryFileCreated(account: Account) = runBlocking {
        var i = 0
        val delayMs = 100L
        val maxDelayMs = 10_000L
        val maxI = maxDelayMs / delayMs
        while (i <= maxI) {
            val recoveryFiles = deviceRecoveryRepository.getRecoveryFiles(account.userId)
            if (recoveryFiles.isNotEmpty()) break
            delay(delayMs)
            i += 1
        }
    }
}
