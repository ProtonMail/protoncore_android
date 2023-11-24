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

package me.proton.core.paymentiap.presentation.usecase

import android.app.Activity
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import io.mockk.MockKAnnotations
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.payment.domain.entity.GoogleBillingResult
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.payment.domain.usecase.LaunchGiapBillingFlow
import me.proton.core.paymentiap.domain.entity.wrap
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LaunchGiapBillingFlowImplTest {
    @MockK
    private lateinit var googleBillingRepository: GoogleBillingRepository<Activity>

    @MockK
    private lateinit var observabilityManager: ObservabilityManager

    private lateinit var tested: LaunchGiapBillingFlowImpl

    private val testProductId = ProductId("product-id")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        justRun { googleBillingRepository.close() }
        justRun { observabilityManager.enqueue(any(), any()) }

        tested = LaunchGiapBillingFlowImpl({ googleBillingRepository }, observabilityManager)
    }

    @Test
    fun `billing service unavailable`() = runTest {
        // GIVEN
        val responseCode = BillingResponseCode.SERVICE_UNAVAILABLE
        val billingResult = makeBillingResult(responseCode)
        val purchaseUpdates = flowOf(Pair(billingResult, null))
        coJustRun { googleBillingRepository.launchBillingFlow(any(), any()) }
        every { googleBillingRepository.purchaseUpdated } returns purchaseUpdates

        // WHEN & THEN
        val error = assertFailsWith<BillingClientError> {
            tested(mockk(), testProductId, mockk())
        }
        assertEquals(responseCode, error.responseCode)
    }

    @Test
    fun `null purchase list`() = runTest {
        // GIVEN
        val responseCode = BillingResponseCode.OK
        val billingResult = makeBillingResult(responseCode)
        val purchaseUpdates = flowOf(Pair(billingResult, null))
        coJustRun { googleBillingRepository.launchBillingFlow(any(), any()) }
        every { googleBillingRepository.purchaseUpdated } returns purchaseUpdates

        // WHEN
        val result = tested(mockk(), testProductId, mockk())

        // THEN
        assertEquals(LaunchGiapBillingFlow.Result.Error.PurchaseNotFound, result)
    }

    @Test
    fun `empty purchase list`() = runTest {
        // GIVEN
        val responseCode = BillingResponseCode.OK
        val billingResult = makeBillingResult(responseCode)
        val purchaseUpdates = flowOf(Pair(billingResult, emptyList<GooglePurchase>()))
        coJustRun { googleBillingRepository.launchBillingFlow(any(), any()) }
        every { googleBillingRepository.purchaseUpdated } returns purchaseUpdates

        // WHEN
        val result = tested(mockk(), testProductId, mockk())

        // THEN
        assertEquals(LaunchGiapBillingFlow.Result.Error.PurchaseNotFound, result)
    }

    @Test
    fun `empty customer ID`() = runTest {
        // GIVEN
        val responseCode = BillingResponseCode.OK
        val billingResult = makeBillingResult(responseCode)
        val purchase = mockk<Purchase> {
            every { accountIdentifiers } returns mockk {
                every { obfuscatedAccountId } returns ""
                every { products } returns listOf(testProductId.id)
                every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            }
        }.wrap()
        val purchaseUpdates = flowOf(Pair(billingResult, listOf(purchase)))
        coJustRun { googleBillingRepository.launchBillingFlow(any(), any()) }
        every { googleBillingRepository.purchaseUpdated } returns purchaseUpdates

        // WHEN
        val result = tested(mockk(), testProductId, mockk())

        // THEN
        assertEquals(LaunchGiapBillingFlow.Result.Error.EmptyCustomerId, result)
    }

    @Test
    fun `null account identifiers`() = runTest {
        // GIVEN
        val responseCode = BillingResponseCode.OK
        val billingResult = makeBillingResult(responseCode)
        val purchase = mockk<Purchase> {
            every { accountIdentifiers } returns null
            every { products } returns listOf(testProductId.id)
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
        }.wrap()
        val purchaseUpdates = flowOf(Pair(billingResult, listOf(purchase)))
        coJustRun { googleBillingRepository.launchBillingFlow(any(), any()) }
        every { googleBillingRepository.purchaseUpdated } returns purchaseUpdates

        // WHEN & THEN
        val error = assertFailsWith<IllegalArgumentException> {
            tested(mockk(), testProductId, mockk())
        }
        assertEquals("The purchase should contain a customer ID.", error.message)
    }

    @Test
    fun `purchase does not contain required product ID`() = runTest {
        // GIVEN
        val responseCode = BillingResponseCode.OK
        val billingResult = makeBillingResult(responseCode)
        val purchase = mockk<Purchase> {
            every { accountIdentifiers } returns mockk {
                every { obfuscatedAccountId } returns "customer-id"
                every { products } returns listOf("unknown-product-id")
            }
        }.wrap()
        val purchaseUpdates = flowOf(Pair(billingResult, listOf(purchase)))
        coJustRun { googleBillingRepository.launchBillingFlow(any(), any()) }
        every { googleBillingRepository.purchaseUpdated } returns purchaseUpdates

        // WHEN
        val result = tested(mockk(), testProductId, mockk())

        // THEN
        assertEquals(LaunchGiapBillingFlow.Result.Error.PurchaseNotFound, result)
    }

    @Test
    fun `successful purchase`() = runTest {
        // GIVEN
        val responseCode = BillingResponseCode.OK
        val billingResult = makeBillingResult(responseCode)
        val purchase = mockk<Purchase> {
            every { accountIdentifiers } returns mockk {
                every { obfuscatedAccountId } returns "customer-id"
                every { products } returns listOf(testProductId.id)
            }
            every { purchaseState } returns Purchase.PurchaseState.PURCHASED
        }.wrap()
        val purchaseUpdates = flowOf(Pair(billingResult, listOf(purchase)))
        coJustRun { googleBillingRepository.launchBillingFlow(any(), any()) }
        every { googleBillingRepository.purchaseUpdated } returns purchaseUpdates

        // WHEN
        val result = tested(mockk(), testProductId, mockk())

        // THEN
        assertEquals(LaunchGiapBillingFlow.Result.PurchaseSuccess(purchase), result)
    }

    private fun makeBillingResult(@BillingResponseCode responseCode: Int): GoogleBillingResult =
        BillingResult.newBuilder().setResponseCode(responseCode).build().wrap()
}
