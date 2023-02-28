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

package me.proton.core.observability.data

import android.content.Context
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsObservabilityEnabledImplTest {

    // region mocks
    private val resources = mockk<Resources>(relaxed = true)
    private val context = mockk<Context>(relaxed = true)
    private lateinit var repository: IsObservabilityEnabled
    // endregion

    @Before
    fun beforeEveryTest() {
        // GIVEN
        every { context.resources } returns resources
        repository = IsObservabilityEnabledImpl(context)
    }

    @Test
    fun `observability enabled returns true`() = runTest {
        // GIVEN
        every { resources.getBoolean(R.bool.core_feature_observability_enabled) } returns true
        // WHEN
        val result = repository.invoke()
        // THEN
        assertTrue(result)
    }

    @Test
    fun `observability disabled returns false`() = runTest {
        // GIVEN
        every { resources.getBoolean(R.bool.core_feature_observability_enabled) } returns false
        // WHEN
        val result = repository.invoke()
        // THEN
        assertFalse(result)
    }
}