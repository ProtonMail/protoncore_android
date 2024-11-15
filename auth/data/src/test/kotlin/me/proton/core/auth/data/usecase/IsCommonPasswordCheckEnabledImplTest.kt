/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.data.usecase

import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.auth.data.R
import me.proton.core.auth.data.feature.IsCommonPasswordCheckEnabledImpl
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import org.junit.Assert.*
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalProtonFeatureFlag::class)
class IsCommonPasswordCheckEnabledImplTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var featureFlagManager: FeatureFlagManager

    @MockK
    private lateinit var resources: Resources

    private val featureId = IsCommonPasswordCheckEnabledImpl.featureId

    private lateinit var tested: IsCommonPasswordCheckEnabledImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        every { featureFlagManager.getValue(any(), any()) } returns false
        tested = IsCommonPasswordCheckEnabledImpl(context, featureFlagManager)
    }

    @Test
    fun commonPasswordCheckEnabled() {
        every { resources.getBoolean(R.bool.core_feature_common_password_check_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns true
        assertTrue(tested(null))
    }

    @Test
    fun commonPasswordCheckDisabled() {
        every { resources.getBoolean(R.bool.core_feature_common_password_check_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns false
        kotlin.test.assertFalse(tested(null))
    }

    @Test
    fun commonPasswordCheckDisabledRemoteDisabled() {
        every { resources.getBoolean(R.bool.core_feature_common_password_check_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns false
        kotlin.test.assertFalse(tested(null))
    }

    @Test
    fun commonPasswordCheckDisabledLocalDisabled() {
        every { resources.getBoolean(R.bool.core_feature_common_password_check_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns true
        kotlin.test.assertFalse(tested(null))
    }
}