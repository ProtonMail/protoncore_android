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

package me.proton.core.test.android.uitests

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.network.data.di.BaseProtonApiUrl
import okhttp3.HttpUrl
import org.junit.Rule

interface BaseMockTest {
    @BaseProtonApiUrl
    var baseProtonApiUrl: HttpUrl

    val testUsername get() = "test-mock-936"
    val testPassword get() = "password"
}

/** A minimal example for writing a test with hilt-testing and mocked server. */
@HiltAndroidTest
class SampleMockTest : BaseMockTest {
    @get:Rule
    val mockTestRule = MockTestRule(this)

    @BindValue
    @BaseProtonApiUrl
    override lateinit var baseProtonApiUrl: HttpUrl
}
