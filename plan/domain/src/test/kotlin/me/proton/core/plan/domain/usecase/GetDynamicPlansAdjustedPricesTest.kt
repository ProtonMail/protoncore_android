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
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.domain.entity.ProductPrice
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.IsDynamicPlanAdjustedPriceEnabled
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.entity.freePlan
import me.proton.core.plan.domain.entity.unlimitedPlan
import me.proton.core.plan.domain.entity.unlimitedPlanNoVendor
import me.proton.core.plan.domain.repository.PlansRepository
import org.junit.Test
import java.util.Optional
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class GetDynamicPlansAdjustedPricesTest {
    @MockK
    private lateinit var plansRepository: PlansRepository

    @MockK
    private lateinit var getAvailablePaymentProviders: GetAvailablePaymentProviders

    @MockK
    private lateinit var storePrices: GetStorePrice

    @MockK
    private lateinit var isDynamicPlanAdjustedPriceEnabled: IsDynamicPlanAdjustedPriceEnabled

    private lateinit var tested: GetDynamicPlansAdjustedPrices

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = GetDynamicPlansAdjustedPrices(
            plansRepository,
            AppStore.GooglePlay,
            getAvailablePaymentProviders,
            Optional.of(storePrices),
            isDynamicPlanAdjustedPriceEnabled
        )

        every { isDynamicPlanAdjustedPriceEnabled.invoke(any()) } returns true
    }

    @Test
    fun `google payment providers exist store price is provided`() = runTest {
        // GIVEN
        coEvery { plansRepository.getDynamicPlans(any(), any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(
                freePlan,
                unlimitedPlan,
            )
        )

        coEvery { getAvailablePaymentProviders.invoke(any()) } returns PaymentProvider.values().toSet()
        coEvery { storePrices.invoke(ProductId("googlemail_plus_1_renewing")) } returns ProductPrice(
            provider = PaymentProvider.GoogleInAppPurchase,
            priceAmountMicros = 1000000,
            currency = "CHF",
            formattedPriceAndCurrency = "CHF 100"
        )

        // WHEN
        val plans = tested(userId = null).plans

        // THEN
        assertEquals(2, plans.size)
        assertEquals(freePlan, plans[0])
        val paidPlan = plans[1]
        val paidPlanInstance = paidPlan.instances[1]
        assertEquals(100, paidPlanInstance?.price?.get("CHF")?.current)
    }

    @Test
    fun `google payment providers exist store price is provided but no vendor available`() = runTest {
        // GIVEN
        coEvery { plansRepository.getDynamicPlans(any(), any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(
                unlimitedPlanNoVendor,
            )
        )

        coEvery { getAvailablePaymentProviders.invoke(any()) } returns PaymentProvider.values().toSet()
        coEvery { storePrices.invoke(ProductId("googlemail_plus_1_renewing")) } returns ProductPrice(
            provider = PaymentProvider.GoogleInAppPurchase,
            priceAmountMicros = 1000000,
            currency = "CHF",
            formattedPriceAndCurrency = "CHF 100"
        )

        // WHEN
        val plans = tested(userId = null).plans

        // THEN
        assertEquals(1, plans.size)
        val paidPlan = plans[0]
        val paidPlanInstance = paidPlan.instances[1]
        assertEquals(499, paidPlanInstance?.price?.get("CHF")?.current)
    }

    @Test
    fun `google payment providers does NOT exist store price is provided`() = runTest {
        // GIVEN
        coEvery { plansRepository.getDynamicPlans(any(), any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(
                freePlan,
                unlimitedPlan,
            )
        )

        coEvery { getAvailablePaymentProviders.invoke(any()) } returns setOf(PaymentProvider.CardPayment)
        coEvery { storePrices.invoke(ProductId("googlemail_plus_1_renewing")) } returns ProductPrice(
            provider = PaymentProvider.GoogleInAppPurchase,
            priceAmountMicros = 1000000,
            currency = "CHF",
            formattedPriceAndCurrency = "CHF 100"
        )

        // WHEN
        val plans = tested(userId = null).plans

        // THEN
        assertEquals(2, plans.size)
        assertEquals(freePlan, plans[0])
        val paidPlan = plans[1]
        assertEquals(unlimitedPlan, paidPlan)
        val paidPlanInstance = paidPlan.instances[1]
        assertEquals(499, paidPlanInstance?.price?.get("CHF")?.current)
    }

    @Test
    fun `google payment providers exist store price is NOT provided`() = runTest {
        // GIVEN
        tested = GetDynamicPlansAdjustedPrices(
            plansRepository,
            AppStore.GooglePlay,
            getAvailablePaymentProviders,
            Optional.empty(),
            isDynamicPlanAdjustedPriceEnabled
        )

        coEvery { plansRepository.getDynamicPlans(any(), any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(
                freePlan,
                unlimitedPlan,
            )
        )

        coEvery { getAvailablePaymentProviders.invoke(any()) } returns PaymentProvider.values().toSet()
        coEvery { storePrices.invoke(ProductId("googlemail_plus_1_renewing")) } returns ProductPrice(
            provider = PaymentProvider.GoogleInAppPurchase,
            priceAmountMicros = 1000000,
            currency = "CHF",
            formattedPriceAndCurrency = "CHF 100"
        )

        // WHEN
        val plans = tested(userId = null).plans

        // THEN
        assertEquals(2, plans.size)
        assertEquals(freePlan, plans[0])
        val paidPlan = plans[1]
        val paidPlanInstance = paidPlan.instances[1]
        assertEquals(499, paidPlanInstance?.price?.get("CHF")?.current)
    }

    @Test
    fun `google payment providers exist store price is provided but no such product on PlayStore`() = runTest {
        // GIVEN
        coEvery { plansRepository.getDynamicPlans(any(), any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(
                freePlan,
                unlimitedPlan,
            )
        )

        coEvery { getAvailablePaymentProviders.invoke(any()) } returns PaymentProvider.values().toSet()
        coEvery { storePrices.invoke(ProductId("googlemail_plus_12_renewing")) } returns ProductPrice(
            provider = PaymentProvider.GoogleInAppPurchase,
            priceAmountMicros = 1000000,
            currency = "CHF",
            formattedPriceAndCurrency = "CHF 100"
        )
        coEvery { storePrices.invoke(ProductId("googlemail_plus_1_renewing")) } returns null

        // WHEN
        val plans = tested(userId = null).plans

        // THEN
        assertEquals(2, plans.size)
        assertEquals(freePlan, plans[0])
        val paidPlan = plans[1]
        val paidPlanInstance = paidPlan.instances[1]
        assertEquals(499, paidPlanInstance?.price?.get("CHF")?.current)
    }

    @Test
    fun `dynamic plans adjusted prices feature flag off`() = runTest {
        // GIVEN
        every { isDynamicPlanAdjustedPriceEnabled.invoke(any()) } returns false

        coEvery { plansRepository.getDynamicPlans(any(), any()) } returns DynamicPlans(
            defaultCycle = null,
            plans = listOf(
                freePlan,
                unlimitedPlan,
            )
        )

        coEvery { getAvailablePaymentProviders.invoke(any()) } returns PaymentProvider.values().toSet()
        coEvery { storePrices.invoke(ProductId("googlemail_plus_1_renewing")) } returns ProductPrice(
            provider = PaymentProvider.GoogleInAppPurchase,
            priceAmountMicros = 1000000,
            currency = "CHF",
            formattedPriceAndCurrency = "CHF 100"
        )

        // WHEN
        val plans = tested(userId = null).plans

        // THEN
        assertEquals(2, plans.size)
        assertEquals(freePlan, plans[0])
        val paidPlan = plans[1]
        val paidPlanInstance = paidPlan.instances[1]
        assertEquals(499, paidPlanInstance?.price?.get("CHF")?.current)
    }
}
