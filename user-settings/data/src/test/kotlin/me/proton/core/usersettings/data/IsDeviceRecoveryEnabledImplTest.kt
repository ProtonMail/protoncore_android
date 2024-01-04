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

package me.proton.core.usersettings.data

import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalProtonFeatureFlag::class)
class IsDeviceRecoveryEnabledImplTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var featureFlagManager: FeatureFlagManager

    private lateinit var tested: IsDeviceRecoveryEnabledImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        tested = IsDeviceRecoveryEnabledImpl(context, featureFlagManager)
    }

    @Test
    fun localEnabledRemoteEnabledReturnEnabled() {
        every { resources.getBoolean(R.bool.core_feature_device_recovery_enabled) } returns true
        every { featureFlagManager.getValue(any(), IsDeviceRecoveryEnabledImpl.featureId) } returns true

        assertTrue(tested(userId = null))
    }

    @Test
    fun localEnabledRemoteDisabledReturnDisabled() {
        every { resources.getBoolean(R.bool.core_feature_device_recovery_enabled) } returns true
        every { featureFlagManager.getValue(any(), IsDeviceRecoveryEnabledImpl.featureId) } returns false

        assertFalse(tested(userId = null))
    }

    @Test
    fun localDisabledRemoteEnabledReturnDisabled() {
        every { resources.getBoolean(R.bool.core_feature_device_recovery_enabled) } returns false
        every { featureFlagManager.getValue(any(), IsDeviceRecoveryEnabledImpl.featureId) } returns true

        assertFalse(tested(userId = null))
    }

    @Test
    fun localDisabledRemoteDisabledReturnDisabled() {
        every { resources.getBoolean(R.bool.core_feature_device_recovery_enabled) } returns false
        every { featureFlagManager.getValue(any(), IsDeviceRecoveryEnabledImpl.featureId) } returns false

        assertFalse(tested(userId = null))
    }
}
