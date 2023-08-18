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

package me.proton.core.util.android.sentry

import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsAccountSentryLoggingEnabledTest {
    private val resources = mockk<Resources>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private lateinit var isAccountSentryLoggingEnabled: IsAccountSentryLoggingEnabled

    @Before
    fun beforeEveryTest() {
        every { context.resources } returns resources
        isAccountSentryLoggingEnabled = IsAccountSentryLoggingEnabled(context)
    }

    @Test
    fun `flag false read properly`() {
        every { resources.getBoolean(R.bool.core_feature_account_sentry_enabled) } returns false
        val result = isAccountSentryLoggingEnabled()
        assertFalse(result)
    }

    @Test
    fun `flag true read properly`() {
        every { resources.getBoolean(R.bool.core_feature_account_sentry_enabled) } returns true
        val result = isAccountSentryLoggingEnabled()
        assertTrue(result)
    }
}