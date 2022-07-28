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
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.Scope
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders.Companion.AllPaymentsDisabled
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders.Companion.GoogleIAPEnabled
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders.Companion.ProtonCardPaymentsEnabled
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetAvailablePaymentProvidersTest {
    private lateinit var accountManager: AccountManager
    private lateinit var featureFlagManager: FeatureFlagManager
    private lateinit var googlePlayBillingLibrary: GooglePlayBillingLibrary
    private lateinit var tested: GetAvailablePaymentProviders

    @BeforeTest
    fun setUp() {
        accountManager = mockk { every { getPrimaryUserId() } returns flowOf(null) }
        featureFlagManager = mockk()
        googlePlayBillingLibrary = mockk()
        tested = GetAvailablePaymentProviders(accountManager, featureFlagManager, googlePlayBillingLibrary)
    }

    @Test
    fun `all payments disabled`() = runBlockingTest {
        mockFeature(AllPaymentsDisabled, true)
        mockFeature(GoogleIAPEnabled, true)
        mockFeature(ProtonCardPaymentsEnabled, true)
        mockGoogleIAP(false)

        assertEquals(
            emptySet(),
            tested()
        )
    }

    @Test
    fun `payments enabled but payment providers disabled`() = runBlockingTest {
        mockFeature(AllPaymentsDisabled, false)
        mockFeature(GoogleIAPEnabled, false)
        mockFeature(ProtonCardPaymentsEnabled, false)
        mockGoogleIAP(true)

        assertEquals(
            emptySet(),
            tested()
        )
    }

    @Test
    fun `payments are disabled even though all providers are available`() = runBlockingTest {
        mockFeature(AllPaymentsDisabled, true)
        mockFeature(GoogleIAPEnabled, true)
        mockFeature(ProtonCardPaymentsEnabled, true)
        mockGoogleIAP(true)

        assertEquals(
            emptySet(),
            tested()
        )
    }

    @Test
    fun `payments enabled and all providers available`() = runBlockingTest {
        mockFeature(AllPaymentsDisabled, false)
        mockFeature(GoogleIAPEnabled, true)
        mockFeature(ProtonCardPaymentsEnabled, true)
        mockGoogleIAP(true)

        assertEquals(
            setOf(PaymentProvider.GoogleInAppPurchase, PaymentProvider.ProtonPayment),
            tested()
        )
    }

    @Test
    fun `all payments enabled but missing Google IAP library`() = runBlockingTest {
        mockFeature(AllPaymentsDisabled, false)
        mockFeature(GoogleIAPEnabled, true)
        mockFeature(ProtonCardPaymentsEnabled, true)
        mockGoogleIAP(false)

        assertEquals(
            setOf(PaymentProvider.ProtonPayment),
            tested()
        )
    }

    @Test
    fun `only Proton Card payments enabled`() = runBlockingTest {
        mockFeature(AllPaymentsDisabled, false)
        mockFeature(GoogleIAPEnabled, false)
        mockFeature(ProtonCardPaymentsEnabled, true)
        mockGoogleIAP(true)

        assertEquals(
            setOf(PaymentProvider.ProtonPayment),
            tested()
        )
    }

    @Test
    fun `only Google IAP enabled`() = runBlockingTest {
        mockFeature(AllPaymentsDisabled, false)
        mockFeature(GoogleIAPEnabled, true)
        mockFeature(ProtonCardPaymentsEnabled, false)
        mockGoogleIAP(true)

        assertEquals(
            setOf(PaymentProvider.GoogleInAppPurchase),
            tested()
        )
    }

    private fun mockFeature(flag: FeatureFlag, value: Boolean) {
        val featureFlag = FeatureFlag(
            userId = null,
            featureId = flag.featureId,
            scope = Scope.Global,
            defaultValue = value,
            value = value
        )
        coEvery { featureFlagManager.getOrDefault(any(), flag.featureId, any()) } returns featureFlag
    }

    private fun mockGoogleIAP(available: Boolean) {
        every { googlePlayBillingLibrary.isAvailable() } returns available
    }
}
