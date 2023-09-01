/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.accountrecovery.test

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import me.proton.core.accountmanager.data.AccountStateHandler
import me.proton.core.accountrecovery.test.robot.AccountRecoveryGracePeriodRobot
import me.proton.core.auth.test.flow.SignInFlow
import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.usecase.WaitForPrimaryAccount
import me.proton.core.domain.entity.UserId
import me.proton.core.eventmanager.domain.EventManager
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.repository.EventMetadataRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.notification.domain.repository.NotificationRepository
import me.proton.core.test.quark.Quark
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

private const val GRACE_PERIOD_NOTIFICATION_TITLE = "Password reset"

/**
 * Note: requires [me.proton.test.fusion.FusionConfig.Compose.testRule] to be initialized.
 */
public interface MinimalAccountRecoveryNotificationTest {
    public val accountStateHandler: AccountStateHandler
    public val apiProvider: ApiProvider
    public val eventManagerProvider: EventManagerProvider
    public val eventMetadataRepository: EventMetadataRepository
    public val notificationRepository: NotificationRepository
    public val quark: Quark
    public val waitForPrimaryAccount: WaitForPrimaryAccount

    /** When called, should verify the app is on the home screen for the logged in user. */
    public fun verifyAfterLogin()

    @BeforeTest
    public fun prepare() {
        quark.jailUnban()
        accountStateHandler.start()
    }

    @AfterTest
    public fun resetDevice() {
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressHome()
    }

    @Test
    public fun receiveAccountRecoveryNotification() {
        val (user, _) = quark.userCreate()

        AddAccountRobot.clickSignIn()
        SignInFlow.signInInternal(user.name, user.password)

        val account = waitForPrimaryAccount()

        runBlocking {
            val eventManagerConfig = EventManagerConfig.Core(account.userId)
            val eventManager = eventManagerProvider.get(eventManagerConfig)
            eventManager.stop()
            eventManager.start()
            waitForInitialEvents(eventManager)

            // Trigger account recovery:
            apiProvider.get<TestApi>(account.userId).invoke {
                startAccountRecovery()
            }

            waitForNotifications(eventManager, account.userId)
            eventManager.stop()
        }

        verifyAfterLogin()

        // Open Notifications tray:
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.pressHome()
        uiDevice.openNotification()
        uiDevice.wait(Until.hasObject(By.textStartsWith(GRACE_PERIOD_NOTIFICATION_TITLE)), 30_000)

        // Click the notification:
        uiDevice.findObject(By.textStartsWith(GRACE_PERIOD_NOTIFICATION_TITLE))?.click()
            ?: error("Could not find the notification for account recovery.")

        // Check that Account recovery popup is displayed:
        val gracePeriodRobot = AccountRecoveryGracePeriodRobot()
        gracePeriodRobot.uiElementsDisplayed()
        gracePeriodRobot.clickContinue()

        verifyAfterLogin()
    }

    /**
     * Note: Since the `WorkManager` is mocked for android tests,
     * we need to loop the event manager manually.
     */
    private suspend fun waitForInitialEvents(eventManager: EventManager) {
        (1..10).onEach {
            eventManager.process()

            // Check if the events are fetched at least once:
            val metadataList = eventMetadataRepository.get(eventManager.config)
            if (metadataList.any { it.eventId != null }) {
                return
            }

            delay(100)
        }
    }

    /**
     * Note: Since the `WorkManager` is mocked for android tests,
     * we need to loop the event manager manually.
     */
    private suspend fun waitForNotifications(eventManager: EventManager, userId: UserId) {
        (1..10).onEach {// limit the iteration number, just in case
            eventManager.process()

            // Check if we have some notifications:
            if (notificationRepository.getAllNotificationsByUser(userId).isNotEmpty()) {
                return
            }

            delay(100)
        }
    }
}
