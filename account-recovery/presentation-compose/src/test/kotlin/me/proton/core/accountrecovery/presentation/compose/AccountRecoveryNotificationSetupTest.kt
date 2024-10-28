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

package me.proton.core.accountrecovery.presentation.compose

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import me.proton.core.accountrecovery.presentation.compose.AccountRecoveryNotificationSetup.Companion.deeplink
import me.proton.core.accountrecovery.presentation.compose.AccountRecoveryNotificationSetup.Companion.type
import me.proton.core.accountrecovery.presentation.compose.ui.AccountRecoveryDialogActivity
import me.proton.core.domain.entity.UserId
import me.proton.core.notification.domain.entity.NotificationId
import me.proton.core.notification.presentation.NotificationDeeplink
import me.proton.core.notification.presentation.deeplink.DeeplinkManager
import me.proton.core.notification.test.deeplink.TestDeeplinkIntentProvider
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class AccountRecoveryNotificationSetupTest {

    private val userId = UserId("userId")
    private val notificationId = NotificationId("notificationId")
    private val provider = TestDeeplinkIntentProvider()

    private lateinit var manager: DeeplinkManager
    private lateinit var tested: AccountRecoveryNotificationSetup

    @Before
    fun before() {
        manager = spyk(DeeplinkManager())
        tested = AccountRecoveryNotificationSetup(manager)
        mockkObject(AccountRecoveryDialogActivity.Companion)
        every { AccountRecoveryDialogActivity.Companion.start(any(), any()) } returns Unit
    }

    @Test
    fun registerAndReceiveOpenDeeplink() {
        val path = deeplink
        val call = NotificationDeeplink.Open.get(userId, notificationId, type)

        manager.register(path) {
            assertEquals(expected = 2, actual = it.args.size)
            assertEquals(expected = userId.id, actual = it.args[0])
            assertEquals(expected = notificationId.id, actual = it.args[1])
            true
        }

        val intent = provider.getActivityIntent(call)
        val handled = manager.handle(intent)
        assertEquals(expected = true, actual = handled)
    }

    @Test
    fun setupRegisterAndReceiveOpenDeeplink() {
        val call = NotificationDeeplink.Open.get(userId, notificationId, type)

        tested.invoke()

        val intent = provider.getActivityIntent(call)
        val handled = manager.handle(intent, mockk())
        assertEquals(expected = true, actual = handled)

        verify(exactly = 1) { AccountRecoveryDialogActivity.Companion.start(any(), any()) }
    }

    @Test
    fun setupRegisterAndReceiveOtherOpenDeeplink() {
        val call = NotificationDeeplink.Open.get(userId, notificationId, "anotherType")

        tested.invoke()

        val intent = provider.getActivityIntent(call)
        val handled = manager.handle(intent, mockk())
        assertEquals(expected = false, actual = handled)

        verify(exactly = 0) { AccountRecoveryDialogActivity.Companion.start(any(), any()) }
    }
}
