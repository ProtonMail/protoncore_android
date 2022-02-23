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

package me.proton.core.payment.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PurchaseEnabledTest {
    // region mocks
    private val featureFlagManager = mockk<FeatureFlagManager>(relaxed = true)
    // endregion

    // region test data
    private val testUserId = UserId("test-user-id")
    private val paymentsAndroidFeatureFlag = FeatureId("PaymentsAndroidEnabled")

    // endregion
    private lateinit var useCase: PurchaseEnabled

    @Before
    fun beforeEveryTest() {
        useCase = PurchaseEnabled(featureFlagManager)
        coEvery {
            featureFlagManager.get(
                userId = null, // we need global property
                featureId = paymentsAndroidFeatureFlag,
                refresh = true
            )
        } returns FeatureFlag(paymentsAndroidFeatureFlag, true)
    }

    @Test
    fun `payment status returns android success`() = runBlockingTest {
        val result = useCase.invoke()
        assertNotNull(result)
        assertTrue(result)
    }

    @Test
    fun `payment status returns android false`() = runBlockingTest {
        coEvery {
            featureFlagManager.get(
                userId = null, // we need global property
                featureId = paymentsAndroidFeatureFlag,
                refresh = true
            )
        } returns FeatureFlag(paymentsAndroidFeatureFlag, false)
        val result = useCase.invoke()
        assertNotNull(result)
        assertFalse(result)
    }
}
