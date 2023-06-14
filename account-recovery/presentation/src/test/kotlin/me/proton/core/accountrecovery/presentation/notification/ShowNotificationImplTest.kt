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
import androidx.test.core.app.ApplicationProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import me.proton.core.accountrecovery.domain.AccountRecoveryState
import me.proton.core.accountrecovery.domain.GetAccountRecoveryChannelId
import me.proton.core.accountrecovery.presentation.internal.GetNotificationId
import me.proton.core.accountrecovery.presentation.internal.GetNotificationTag
import me.proton.core.accountrecovery.presentation.internal.HasNotificationPermission
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test

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
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun noNotificationPermission() {
        // GIVEN
        makeTested()
        every { hasNotificationPermission() } returns false

        // WHEN
        tested(AccountRecoveryState.ResetPassword, UserId("user-id"))

        // THEN
        verify(exactly = 0) { getNotificationId() }
        verify(exactly = 0) { getNotificationTag(any()) }
    }

    @Test
    fun voidState() {
        // GIVEN
        makeTested()
        every { hasNotificationPermission() } returns true

        // WHEN
        tested(AccountRecoveryState.None, UserId("user-id"))

        // THEN
        verify(exactly = 0) { getNotificationId() }
        verify(exactly = 0) { getNotificationTag(any()) }
    }

    @Test
    fun postNotification() {
        // GIVEN
        val userId = UserId("user-id")
        makeTested()

        every { hasNotificationPermission() } returns true
        every { getAccountRecoveryChannelId() } returns "channel-id"
        every { getNotificationId() } returns 1
        every { getNotificationTag(any()) } returns "notification-tag"

        // WHEN
        tested(AccountRecoveryState.ResetPassword, userId)

        // THEN
        verify { getNotificationId() }
        verify { getNotificationTag(userId) }
    }

    private fun makeTested(product: Product = Product.Mail) {
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