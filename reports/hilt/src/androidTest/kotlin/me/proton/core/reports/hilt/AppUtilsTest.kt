/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.reports.hilt

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class AppUtilsTest {
    private lateinit var tested: AppUtils

    @Before
    fun setUp() {
        tested = AppUtils(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun hasValidAppName() {
        assertEquals("ReportsHiltTestApp", tested.appName())
    }

    @Test
    fun hasValidVersionName() {
        assertEquals("1.2.3", tested.appVersionName())
    }
}
