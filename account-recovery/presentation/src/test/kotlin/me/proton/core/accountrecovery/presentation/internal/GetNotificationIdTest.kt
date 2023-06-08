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

package me.proton.core.accountrecovery.presentation.internal

import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.accountrecovery.presentation.R
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class GetNotificationIdTest {

    private val resources = mockk<Resources>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private lateinit var getNotificationId: GetNotificationId

    @Before
    fun beforeEveryTest() {
        every { context.resources } returns resources
        getNotificationId = GetNotificationId(context)
    }

    @Test
    fun `get notification id works correctly`() {
        val testResourceId = 5
        every { resources.getInteger(R.integer.core_feature_account_recovery_notification_id) } returns testResourceId
        val result = getNotificationId()
        assertEquals(testResourceId, result)
        verify { resources.getInteger(R.integer.core_feature_account_recovery_notification_id) }
    }
}