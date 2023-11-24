/*
 * Copyright (c) 2023 Proton AG
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

import android.app.Activity
import android.text.TextUtils
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.ProductDetails
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.PrepareGiapPurchase
import me.proton.core.paymentiap.domain.entity.wrap
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class PrepareGiapPurchaseImplTest {
    @MockK
    private lateinit var findUnacknowledgedGooglePurchase: FindUnacknowledgedGooglePurchase

    @MockK
    private lateinit var googleBillingRepository: GoogleBillingRepository<Activity>

    private lateinit var tested: PrepareGiapPurchaseImpl

    private val testCustomerId = "customer-id"
    private val testProductId = ProductId("product-id")
    private val testProductDetails = mockk<ProductDetails> {
        every { oneTimePurchaseOfferDetails } returns null
        every { productId } returns testProductId.id
        every { productType } returns ProductType.SUBS
        every { subscriptionOfferDetails } returns listOf(mockk {
            every { offerToken } returns "offer-token"
        })
        every { zza() } returns "packageName"
    }.wrap()

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } returns true

        justRun { googleBillingRepository.close() }

        tested = PrepareGiapPurchaseImpl(
            { googleBillingRepository },
            findUnacknowledgedGooglePurchase
        )
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `null product details`() = runTest {
        // GIVEN
        coEvery { googleBillingRepository.getProductsDetails(any()) } returns null

        // WHEN
        val result = tested(testCustomerId, testProductId)

        // THEN
        assertEquals(PrepareGiapPurchase.Result.ProductDetailsNotFound, result)
    }

    @Test
    fun `empty product details`() = runTest {
        // GIVEN
        coEvery { googleBillingRepository.getProductsDetails(any()) } returns emptyList()

        // WHEN
        val result = tested(testCustomerId, testProductId)

        // THEN
        assertEquals(PrepareGiapPurchase.Result.ProductDetailsNotFound, result)
    }

    @Test
    fun `unredeemed purchase`() = runTest {
        // GIVEN
        coEvery { googleBillingRepository.getProductsDetails(any()) } returns listOf(
            testProductDetails
        )
        val purchase = mockk<GooglePurchase>()
        coEvery { findUnacknowledgedGooglePurchase.byProduct(any()) } returns purchase

        // WHEN
        val result = tested(testCustomerId, testProductId)

        // THEN
        assertEquals(PrepareGiapPurchase.Result.Unredeemed(purchase), result)
    }

    @Test
    fun `missing subscription offer details`() = runTest {
        // GIVEN
        val productDetails = mockk<ProductDetails> {
            every { productId } returns testProductId.id
            every { subscriptionOfferDetails } returns null
        }.wrap()
        coEvery { googleBillingRepository.getProductsDetails(any()) } returns listOf(productDetails)
        coEvery { findUnacknowledgedGooglePurchase.byProduct(any()) } returns null

        // WHEN & THEN
        assertFailsWith<IllegalArgumentException> {
            tested(testCustomerId, testProductId)
        }
    }

    @Test
    fun `successful result`() = runTest {
        // GIVEN
        coEvery { googleBillingRepository.getProductsDetails(any()) } returns listOf(
            testProductDetails
        )
        coEvery { findUnacknowledgedGooglePurchase.byProduct(any()) } returns null

        // WHEN
        val result = tested(testCustomerId, testProductId)

        // THEN
        assertIs<PrepareGiapPurchase.Result.Success>(result)
    }
}
