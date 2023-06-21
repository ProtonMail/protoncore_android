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

package me.proton.core.accountrecovery.presentation.notification

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.graphics.drawable.IconCompat
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import me.proton.core.accountrecovery.domain.GetAccountRecoveryChannelId
import me.proton.core.accountrecovery.presentation.R
import me.proton.core.accountrecovery.presentation.internal.GetNotificationId
import me.proton.core.accountrecovery.presentation.internal.GetNotificationTag
import me.proton.core.accountrecovery.presentation.internal.HasNotificationPermission
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserRecovery
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class ShowNotificationImplTest {
    private lateinit var context: Context

    @MockK
    private lateinit var getAccountRecoveryChannelId: GetAccountRecoveryChannelId

    @MockK
    private lateinit var getNotificationId: GetNotificationId

    @MockK
    private lateinit var getNotificationTag: GetNotificationTag

    @MockK
    private lateinit var hasNotificationPermission: HasNotificationPermission

    private lateinit var tested: ShowNotificationImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(IconCompat::class)
        mockkStatic("me.proton.core.accountrecovery.presentation.notification.ShowNotificationImplKt")
        context = ApplicationProvider.getApplicationContext()
    }

    @AfterTest
    fun afterEveryTest() {
        unmockkStatic(IconCompat::class)
        unmockkStatic("me.proton.core.accountrecovery.presentation.notification.ShowNotificationImplKt")
    }

    @Test
    fun noNotificationPermission() {
        // GIVEN
        makeTested(context = context)
        every { hasNotificationPermission() } returns false

        // WHEN
        tested(UserRecovery.State.Insecure, UserId("user-id"))

        // THEN
        verify(exactly = 0) { getNotificationId() }
        verify(exactly = 0) { getNotificationTag(any()) }
    }

    @Test
    fun voidState() {
        // GIVEN
        makeTested(context = context)
        every { hasNotificationPermission() } returns true

        // WHEN
        tested(UserRecovery.State.None, UserId("user-id"))

        // THEN
        verify(exactly = 0) { getNotificationId() }
        verify(exactly = 0) { getNotificationTag(any()) }
    }

    @Test
    fun postNotification() {
        // GIVEN
        val userId = UserId("user-id")
        makeTested(context = context)

        every { hasNotificationPermission() } returns true
        every { getAccountRecoveryChannelId() } returns "channel-id"
        every { getNotificationId() } returns 1
        every { getNotificationTag(any()) } returns "notification-tag"

        // WHEN
        tested(UserRecovery.State.Insecure, userId)

        // THEN
        verify { getNotificationId() }
        verify { getNotificationTag(userId) }
    }

    @Test
    fun postNotificationResetPasswordStateTextTest() {
        // GIVEN
        val userId = UserId("user-id")
        val testState = UserRecovery.State.Insecure
        val packageManager = spyk<PackageManager>()
        val contextSpy = spyk(context)
        val stringResourceSlot = mutableListOf<Int>()

        every { contextSpy.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(any()) } returns mockk(relaxed = true)
        every { IconCompat.createWithResource(any(), any()) } returns mockk(relaxed = true)

        every { contextSpy.getString(capture(stringResourceSlot)) } returns "test3"

        makeTested(context = contextSpy)

        every { hasNotificationPermission() } returns true
        every { getAccountRecoveryChannelId() } returns "channel-id"
        every { getNotificationId() } returns 1
        every { getNotificationTag(any()) } returns "notification-tag"

        // WHEN
        tested(testState, userId)

        // THEN
        assertEquals(4, stringResourceSlot.size)
        assertTrue(stringResourceSlot.contains(R.string.account_recovery_notification_content_reset_password))
        assertEquals(R.string.account_recovery_notification_channel_name, stringResourceSlot[0])
        assertEquals(R.string.account_recovery_notification_content_reset_password, stringResourceSlot[1])
        assertEquals(R.string.account_recovery_notification_action_dismiss, stringResourceSlot[2])
        assertEquals(R.string.account_recovery_notification_action_learn_more, stringResourceSlot[3])
        verify { getNotificationId() }
        verify { getNotificationTag(userId) }
    }

    @Test
    fun postNotificationGracePeriodStateTextTest() {
        // GIVEN
        val userId = UserId("user-id")
        val testState = UserRecovery.State.Grace
        val packageManager = spyk<PackageManager>()
        val contextSpy = spyk(context)
        val stringResourceSlot = mutableListOf<Int>()

        every { contextSpy.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(any()) } returns mockk(relaxed = true)
        every { IconCompat.createWithResource(any(), any()) } returns mockk(relaxed = true)

        every { contextSpy.getString(capture(stringResourceSlot)) } returns "test3"

        makeTested(context = contextSpy)

        every { hasNotificationPermission() } returns true
        every { getAccountRecoveryChannelId() } returns "channel-id"
        every { getNotificationId() } returns 1
        every { getNotificationTag(any()) } returns "notification-tag"

        // WHEN
        tested(testState, userId)

        // THEN
        assertEquals(4, stringResourceSlot.size)
        assertTrue(stringResourceSlot.contains(R.string.account_recovery_notification_content_grace_period))
        assertEquals(R.string.account_recovery_notification_channel_name, stringResourceSlot[0])
        assertEquals(R.string.account_recovery_notification_content_grace_period, stringResourceSlot[1])
        assertEquals(R.string.account_recovery_notification_action_dismiss, stringResourceSlot[2])
        assertEquals(R.string.account_recovery_notification_action_learn_more, stringResourceSlot[3])
        verify { getNotificationId() }
        verify { getNotificationTag(userId) }
    }

    @Test
    fun postNotificationVoidStateTextTest() {
        // GIVEN
        val userId = UserId("user-id")
        val testState = UserRecovery.State.None
        val packageManager = spyk<PackageManager>()
        val contextSpy = spyk(context)
        val stringResourceSlot = mutableListOf<Int>()

        every { contextSpy.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(any()) } returns mockk(relaxed = true)
        every { IconCompat.createWithResource(any(), any()) } returns mockk(relaxed = true)

        every { contextSpy.getString(capture(stringResourceSlot)) } returns "test3"

        makeTested(context = contextSpy)

        every { hasNotificationPermission() } returns true
        every { getAccountRecoveryChannelId() } returns "channel-id"
        every { getNotificationId() } returns 1
        every { getNotificationTag(any()) } returns "notification-tag"

        // WHEN
        tested(testState, userId)

        // THEN
        assertEquals(0, stringResourceSlot.size)
        verify(exactly = 0) { getNotificationId() }
        verify(exactly = 0) { getNotificationTag(userId) }
    }

    @Test
    fun postNotificationCancelledStateTextTest() {
        // GIVEN
        val userId = UserId("user-id")
        val testState = UserRecovery.State.Cancelled
        val packageManager = spyk<PackageManager>()
        val contextSpy = spyk(context)
        val stringResourceSlot = mutableListOf<Int>()

        every { contextSpy.packageManager } returns packageManager
        every { packageManager.getLaunchIntentForPackage(any()) } returns mockk(relaxed = true)
        every { IconCompat.createWithResource(any(), any()) } returns mockk(relaxed = true)

        every { contextSpy.getString(capture(stringResourceSlot)) } returns "test3"

        makeTested(context = contextSpy)

        every { hasNotificationPermission() } returns true
        every { getAccountRecoveryChannelId() } returns "channel-id"
        every { getNotificationId() } returns 1
        every { getNotificationTag(any()) } returns "notification-tag"

        // WHEN
        tested(testState, userId)

        // THEN
        assertEquals(4, stringResourceSlot.size)
        assertTrue(stringResourceSlot.contains(R.string.account_recovery_notification_content_cancelled))
        assertEquals(R.string.account_recovery_notification_channel_name, stringResourceSlot[0])
        assertEquals(R.string.account_recovery_notification_content_cancelled, stringResourceSlot[1])
        assertEquals(R.string.account_recovery_notification_action_dismiss, stringResourceSlot[2])
        assertEquals(R.string.account_recovery_notification_action_learn_more, stringResourceSlot[3])
        verify { getNotificationId() }
        verify { getNotificationTag(userId) }
    }

    private fun makeTested(product: Product = Product.Mail, context: Context) {
        tested = ShowNotificationImpl(
            context,
            getAccountRecoveryChannelId,
            getNotificationId,
            getNotificationTag,
            hasNotificationPermission,
            product
        )
    }
}