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

package me.proton.core.paymentiap.data.usecase

import com.android.billingclient.api.BillingClient.BillingResponseCode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.paymentiap.domain.getProductPrice
import me.proton.core.paymentiap.domain.repository.BillingClientError
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import org.junit.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetStorePriceTest {

    private lateinit var googleBillingRepository: GoogleBillingRepository
    private lateinit var tested: GetStorePrice

    @BeforeTest
    fun beforeEveryTest() {
        mockkStatic("me.proton.core.paymentiap.domain.GoogleBillingExtensionsKt")
        googleBillingRepository = mockk(relaxed = true)
        tested = GetStorePrice { googleBillingRepository }
    }

    @AfterTest
    fun afterEveryTest() {
        unmockkStatic("me.proton.core.paymentiap.domain.GoogleBillingExtensionsKt")
    }

    @Test
    fun `billing repository returns empty`() = runTest {
        val testPlanName = ProductId("test-plan-name")
        coEvery { googleBillingRepository.getProductsDetails(any()) } returns emptyList()
        val result = tested(testPlanName)
        assertNull(result)
    }

    @Test
    fun `billing repository returns non empty`() = runTest {
        val testPlanName = ProductId("test-plan-name")
        val productDetails = mockk<com.android.billingclient.api.ProductDetails>(relaxed = true)
        every { productDetails.getProductPrice() } returns mockk {
            every { priceAmountMicros } returns 1000000
            every { formattedPrice } returns "CHF 100"
            every { priceCurrencyCode } returns "CHF"
        }
        coEvery { googleBillingRepository.getProductsDetails(any()) } returns listOf(
            productDetails
        )
        val result = tested(testPlanName)
        assertNotNull(result)
        assertEquals(1000000, result.priceAmountMicros)
        assertEquals("CHF", result.currency)
        assertEquals("CHF 100", result.formattedPriceAndCurrency)
    }

    @Test
    fun `billing repository returns non empty and no prices`() = runTest {
        val testPlanName = ProductId("test-plan-name")
        val productDetails = mockk<com.android.billingclient.api.ProductDetails>(relaxed = true)
        every { productDetails.getProductPrice() } returns null
        coEvery { googleBillingRepository.getProductsDetails(any()) } returns listOf(
            productDetails
        )
        val result = tested(testPlanName)
        assertNull(result)
    }

    @Test
    fun `billing repository throws exception`() = runTest {
        val testPlanName = ProductId("test-plan-name")
        val productDetails = mockk<com.android.billingclient.api.ProductDetails>(relaxed = true)
        every { productDetails.getProductPrice() } returns null
        coEvery { googleBillingRepository.getProductsDetails(any()) } throws BillingClientError(
            BillingResponseCode.SERVICE_TIMEOUT, "Service timeout"
        )
        val error = assertFailsWith<BillingClientError> {
            tested(testPlanName)
        }
        assertNotNull(error)
        assertEquals(BillingResponseCode.SERVICE_TIMEOUT, error.responseCode)
    }
}