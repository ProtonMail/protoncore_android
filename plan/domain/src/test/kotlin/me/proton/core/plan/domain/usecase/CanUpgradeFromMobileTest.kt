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

package me.proton.core.plan.domain.usecase

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.GoogleServicesAvailability
import me.proton.core.payment.domain.usecase.GoogleServicesUtils
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.entity.SubscriptionManagement
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanUpgradeFromMobileTest {

    @MockK
    private lateinit var getAvailablePaymentProviders: GetAvailablePaymentProviders

    @MockK(relaxed = true)
    private lateinit var getCurrentSubscription: GetDynamicSubscription

    @MockK
    private lateinit var googleServicesUtils: GoogleServicesUtils

    @MockK
    private lateinit var optionalGoogleServicesUtils: Optional<GoogleServicesUtils>

    private val testUserId = UserId("user-id")
    private lateinit var useCase: CanUpgradeFromMobile

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        useCase = CanUpgradeFromMobile(
            supportPaidPlans = true,
            getAvailablePaymentProviders = getAvailablePaymentProviders,
            getCurrentSubscription = getCurrentSubscription,
            googleServicesUtils = optionalGoogleServicesUtils
        )
    }

    @Test
    fun `can upgrade returns false when support paid is false`() = runTest {
        // GIVEN
        useCase = CanUpgradeFromMobile(
            supportPaidPlans = false,
            getAvailablePaymentProviders = getAvailablePaymentProviders,
            getCurrentSubscription = getCurrentSubscription,
            googleServicesUtils = optionalGoogleServicesUtils
        )
        // WHEN
        val result = useCase(testUserId)
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns false when no payment providers available`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders() } returns emptySet()
        // WHEN
        val result = useCase(testUserId)
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns false when only PayPal payment provider is available`() = runTest {
        // GIVEN
        coEvery { getAvailablePaymentProviders() } returns setOf(PaymentProvider.PayPal)
        // WHEN
        val result = useCase(testUserId)
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns true for Google Managed Subscription when payment providers available`() = runTest {
        // GIVEN
        every { optionalGoogleServicesUtils.getOrNull() } returns googleServicesUtils
        every { googleServicesUtils.isGooglePlayServicesAvailable() } returns GoogleServicesAvailability.Success
        coEvery { getCurrentSubscription(testUserId) } returns mockk {
            every { external } returns SubscriptionManagement.GOOGLE_MANAGED
        }
        coEvery { getAvailablePaymentProviders() } returns setOf(
            PaymentProvider.CardPayment,
            PaymentProvider.GoogleInAppPurchase
        )
        // WHEN
        val result = useCase(testUserId)
        // THEN
        assertTrue(result)
    }

    @Test
    fun `cannot upgrade when Google Play Services not available`() = runTest {
        // GIVEN
        every { optionalGoogleServicesUtils.getOrNull() } returns googleServicesUtils
        every { googleServicesUtils.isGooglePlayServicesAvailable() } returns GoogleServicesAvailability.ServiceInvalid
        coEvery { getCurrentSubscription(testUserId) } returns mockk {
            every { external } returns SubscriptionManagement.GOOGLE_MANAGED
        }
        coEvery { getAvailablePaymentProviders() } returns setOf(
            PaymentProvider.GoogleInAppPurchase
        )
        // WHEN
        val result = useCase(testUserId)
        // THEN
        assertFalse(result)
    }

    @Test
    fun `can upgrade returns false for Proton Managed when payment providers available`() = runTest {
        // GIVEN
        coEvery { getCurrentSubscription(testUserId) } returns mockk {
            every { external } returns SubscriptionManagement.PROTON_MANAGED
        }
        coEvery { getAvailablePaymentProviders() } returns setOf(
            PaymentProvider.CardPayment,
            PaymentProvider.GoogleInAppPurchase
        )
        // WHEN
        val result = useCase(testUserId)
        // THEN
        assertFalse(result)
    }
}
