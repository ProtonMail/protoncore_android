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

package me.proton.core.notification.presentation.usecase

import android.app.NotificationManager
import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConfigureNotificationChannelImplTest {
    @MockK
    private lateinit var context: Context
    private lateinit var tested: ConfigureNotificationChannelImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ConfigureNotificationChannelImpl(context)
    }

    @Test
    fun configureChannel() {
        val notificationManager = mockk<NotificationManager>()
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.getString(any()) } returns "Channel name"
        tested("channel-id")
    }
}
