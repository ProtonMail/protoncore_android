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
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.ResponseCodes
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.entity.ProductPrice
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.entity.dynamicSubscription
import me.proton.core.plan.domain.entity.dynamicSubscriptionPaid
import me.proton.core.plan.domain.entity.dynamicSubscriptionPaidProtonManaged
import me.proton.core.plan.domain.repository.PlansRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.Optional
import kotlin.test.assertFailsWith

class GetDynamicSubscriptionAdjustedPricesTest {
    // region mocks
    @MockK
    private lateinit var getProductIdForCurrentSubscription: GetProductIdForCurrentSubscription

    @MockK
    private lateinit var storePrices: GetStorePrice

    @MockK
    private lateinit var repository: PlansRepository
    // endregion

    // region test data
    private val testProductId = ProductId("googlemail_plus_1_renewing")
    private val testUserId = UserId("test-user-id")
    private val testSubscription = dynamicSubscription
    private val testPaidSubscription = dynamicSubscriptionPaid
    private val testPaidSubscriptionProtonManaged = dynamicSubscriptionPaidProtonManaged
    private val testSubscriptions = listOf(testSubscription)
    private val testPaidSubscriptions = listOf(testPaidSubscription)
    private val testPaidSubscriptionsProtonManaged = listOf(testPaidSubscriptionProtonManaged)
    // endregion

    private lateinit var useCase: GetDynamicSubscriptionAdjustedPrices
    private lateinit var optionalStorePrices: Optional<GetStorePrice>

    @Before
    fun beforeEveryTest() {
        MockKAnnotations.init(this)
        optionalStorePrices = mockk(relaxed = true)
        useCase = GetDynamicSubscriptionAdjustedPrices(
            repository,
            optionalStorePrices,
            getProductIdForCurrentSubscription
        )
    }

    @Test
    fun `get subscription returns success dyn plans prices disabled`() = runTest {
        // GIVEN
        coEvery { optionalStorePrices.isPresent } returns false
        coEvery { repository.getDynamicSubscriptions(testUserId) } returns testSubscriptions
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertEquals(testSubscription, result)
        assertNotNull(result)
        assertEquals(0L, result.amount)
    }

    @Test
    fun `get subscription returns success all enabled`() = runTest {
        // GIVEN
        coEvery { optionalStorePrices.isPresent } returns true
        every { optionalStorePrices.get() } returns storePrices
        coEvery { storePrices.invoke(testProductId) } returns ProductPrice(
            provider = PaymentProvider.GoogleInAppPurchase,
            priceAmountMicros = 1000000,
            currency = "USD",
            formattedPriceAndCurrency = "USD 100"
        )
        coEvery { getProductIdForCurrentSubscription(testUserId) } returns testProductId
        coEvery { repository.getDynamicSubscriptions(testUserId) } returns testPaidSubscriptions
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertEquals(testPaidSubscription.copy(amount = 100L, currency = "USD"), result)
    }

    @Test
    fun `get subscription returns success all enabled but plan not found`() = runTest {
        // GIVEN
        coEvery { optionalStorePrices.isPresent } returns true
        every { optionalStorePrices.get() } returns storePrices
        coEvery { storePrices.invoke(testProductId) } returns ProductPrice(
            provider = PaymentProvider.GoogleInAppPurchase,
            priceAmountMicros = 1000000,
            currency = "CHF",
            formattedPriceAndCurrency = "CHF 100"
        )
        coEvery { getProductIdForCurrentSubscription(testUserId) } returns null
        coEvery { repository.getDynamicSubscriptions(testUserId) } returns testPaidSubscriptions
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertEquals(testPaidSubscription.copy(amount = null, currency = null), result)
    }

    @Test
    fun `get subscription returns proton managed subscription`() = runTest {
        // GIVEN
        coEvery { optionalStorePrices.isPresent } returns true
        every { optionalStorePrices.get() } returns storePrices
        coEvery { repository.getDynamicSubscriptions(testUserId) } returns testPaidSubscriptionsProtonManaged
        coEvery { getProductIdForCurrentSubscription(testUserId) } returns testProductId
        coEvery { storePrices.invoke(testProductId) } returns ProductPrice(
            provider = PaymentProvider.GoogleInAppPurchase,
            priceAmountMicros = 1000000,
            currency = "CHF",
            formattedPriceAndCurrency = "CHF 100"
        )
        // WHEN
        val result = useCase.invoke(testUserId)
        // THEN
        assertEquals(testPaidSubscriptionProtonManaged, result)
        assertNotNull(result)
        assertEquals(0L, result.amount)
    }

    @Test
    fun `get subscription returns error`() = runTest {
        // GIVEN
        coEvery { repository.getDynamicSubscriptions(testUserId) } throws ApiException(
            ApiResult.Error.Connection(
                false,
                RuntimeException("Test error")
            )
        )
        // WHEN
        val throwable = assertFailsWith(ApiException::class) {
            useCase.invoke(testUserId)
        }
        // THEN
        assertNotNull(throwable)
        assertEquals("Test error", throwable.message)
    }

    @Test
    fun `get dynamic subscription returns no active subscription`() = runTest {
        // GIVEN
        coEvery { repository.getDynamicSubscriptions(testUserId) } throws ApiException(
            ApiResult.Error.Http(
                httpCode = 123,
                "http error",
                ApiResult.Error.ProtonData(
                    code = ResponseCodes.PAYMENTS_SUBSCRIPTION_NOT_EXISTS,
                    error = "no active subscription"
                )
            )
        )
        // WHEN
        assertFailsWith<ApiException> { useCase.invoke(testUserId) }
    }
}
