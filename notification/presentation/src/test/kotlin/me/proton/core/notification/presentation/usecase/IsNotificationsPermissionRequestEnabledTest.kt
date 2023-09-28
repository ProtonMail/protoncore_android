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

import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.proton.core.notification.presentation.R

class IsNotificationsPermissionRequestEnabledTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    private lateinit var tested: IsNotificationsPermissionRequestEnabledImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        tested = IsNotificationsPermissionRequestEnabledImpl(context)
    }

    @Test
    fun notificationsEnabled() {
        every { resources.getBoolean(R.bool.core_feature_notifications_permission_request_enabled) } returns true
        assertTrue(tested())
    }

    @Test
    fun notificationsDisabled() {
        every { resources.getBoolean(R.bool.core_feature_notifications_permission_request_enabled) } returns false
        assertFalse(tested())
    }
}
