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
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

internal class GetNotificationTagTest {

    private val testUserId = "test-user-id"
    private lateinit var getNotificationTag: GetNotificationTag

    @Before
    fun beforeEveryTest() {
        getNotificationTag = GetNotificationTag()
    }

    @Test
    fun `get notification tag works correctly`() {
        val result = getNotificationTag(UserId(testUserId))
        assertEquals("accountRecovery-${testUserId}", result)
    }
}