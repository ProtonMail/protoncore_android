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

package me.proton.core.paymentiap.presentation.viewmodel

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.AppStore
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingProductQueryTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingPurchaseTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingUnredeemedTotalV1
import me.proton.core.observability.domain.metrics.common.GiapStatus
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.presentation.entity.BillingInput
import me.proton.core.payment.presentation.entity.PaymentVendorDetails
import me.proton.core.paymentiap.domain.repository.BillingClientError
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import me.proton.core.test.kotlin.CoroutinesTest
import me.proton.core.util.kotlin.coroutine.result
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BillingIAPViewModelTest : CoroutinesTest by CoroutinesTest() {

    @RelaxedMockK
    private lateinit var billingRepository: GoogleBillingRepository

    @RelaxedMockK
    private lateinit var observabilityManager: ObservabilityManager

    @MockK
    private lateinit var findUnacknowledgedGooglePurchase: FindUnacknowledgedGooglePurchase

    private lateinit var tested: BillingIAPViewModel

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { billingRepository.getProductsDetails(any()) } coAnswers {
            result("getProductDetails") {
                firstArg<List<String>>().map { id -> mockk { every { productId } returns id } }
            }
        }

        tested = createViewModel()
    }

    private fun createViewModel() = BillingIAPViewModel(
        billingRepository,
        findUnacknowledgedGooglePurchase,
        observabilityManager,
        mockk(relaxed = true)
    )

    @Test
    fun `observability data is recorded for product query`() = coroutinesTest {
        // WHEN
        tested.queryProductDetails("test-plan-name").join()

        // THEN
        val dataSlot = slot<CheckoutGiapBillingProductQueryTotal>()
        verify { observabilityManager.enqueue(capture(dataSlot), any()) }
        assertEquals(GiapStatus.success, dataSlot.captured.Labels.status)
    }

    @Test
    fun `observability data is recorded unredeemed purchase is returned`() = coroutinesTest {
        // GIVEN
        val productId = "test-plan-name"
        coEvery { findUnacknowledgedGooglePurchase.byProduct(any()) } returns mockk()
        tested.queryProductDetails(productId).join()

        // WHEN
        val billingInput = mockk<BillingInput> {
            every { plan.vendors } returns mapOf(
                AppStore.GooglePlay to PaymentVendorDetails("", productId)
            )
        }
        tested.makePurchase(mockk(), billingInput).join()

        // THEN
        verify { observabilityManager.enqueue(any<CheckoutGiapBillingUnredeemedTotalV1>(), any()) }
    }

    @Test
    fun `observability data is recorded on purchase result`() = coroutinesTest {
        // GIVEN
        every { billingRepository.purchaseUpdated } returns flowOf(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.DEVELOPER_ERROR)
                .build() to null
        )

        // WHEN
        tested = createViewModel()
        // wait until `onPurchasesUpdated` is called:
        tested.billingIAPState.first { it is BillingIAPViewModel.State.Error.ProductPurchaseError.Message }

        // THEN
        val dataSlot = slot<CheckoutGiapBillingPurchaseTotal>()
        verify { observabilityManager.enqueue(capture(dataSlot), any()) }
        assertEquals(
            CheckoutGiapBillingPurchaseTotal.PurchaseStatus.developerError,
            dataSlot.captured.Labels.status
        )
    }

    @Test
    fun `query for a product that does not exist`() = coroutinesTest {
        // GIVEN
        coEvery { billingRepository.getProductsDetails(any()) } returns null

        // WHEN
        tested.queryProductDetails("test-plan-name").join()

        // THEN
        assertEquals(
            BillingIAPViewModel.State.Error.ProductDetailsError.ProductMismatch,
            tested.billingIAPState.value
        )
    }

    @Test
    fun `collector gets QueryingProductDetails before instant error`() = coroutinesTest {
        // GIVEN
        coEvery { billingRepository.getProductsDetails(any()) } returns null

        val recordedStates = mutableListOf<BillingIAPViewModel.State>()
        val collectJob = launch {
            tested.billingIAPState.collect {
                recordedStates.add(it)
            }
        }

        // WHEN
        tested.queryProductDetails("test-plan-name").join()
        collectJob.cancel()

        // THEN
        assertEquals(
            listOf(
                BillingIAPViewModel.State.Initializing,
                BillingIAPViewModel.State.QueryingProductDetails,
                BillingIAPViewModel.State.Error.ProductDetailsError.ProductMismatch,
            ),
            recordedStates
        )
    }

    @Test
    fun `feature not supported when querying for a product`() = coroutinesTest {
        // GIVEN
        coEvery { billingRepository.getProductsDetails(any()) } throws BillingClientError(
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED, "Feature not supported"
        )

        // WHEN
        tested.queryProductDetails("test-plan-name").join()

        // THEN
        assertEquals(
            BillingIAPViewModel.State.Error.ProductDetailsError.ResponseCode,
            tested.billingIAPState.value
        )
    }

    @Test
    fun `purchase product that was not queried`() = coroutinesTest {
        // WHEN
        tested.queryProductDetails("test-plan-name").join()
        val billingInput = mockk<BillingInput> {
            every { plan.vendors } returns mapOf(
                AppStore.GooglePlay to PaymentVendorDetails("", "unknown-plan-name")
            )
        }
        tested.makePurchase(mockk(), billingInput).join()

        // THEN
        val state = tested.billingIAPState.value
        assertTrue(
            state is BillingIAPViewModel.State.Error.ProductDetailsError.Message
                && "unknown-plan-name" in requireNotNull(state.error)
        )
    }

    @Test
    fun `customerId is not matching`() = coroutinesTest {
        // GIVEN
        val googleProductId = "google-product"
        val purchaseUpdatedFlow = MutableSharedFlow<Pair<BillingResult, List<Purchase>?>>()
        every { billingRepository.purchaseUpdated } returns purchaseUpdatedFlow
        coEvery { findUnacknowledgedGooglePurchase.byProduct(any()) } returns null

        tested = createViewModel()
        tested.queryProductDetails(googleProductId).join()

        // WHEN
        val billingInput = mockk<BillingInput> {
            every { plan } returns mockk {
                every { vendors } returns mapOf(
                    AppStore.GooglePlay to PaymentVendorDetails(
                        customerId = "customerId",
                        vendorPlanName = googleProductId
                    )
                )
            }
        }
        tested.makePurchase(mockk(), billingInput).join()

        purchaseUpdatedFlow.emit(
            BillingResult.newBuilder()
                .setResponseCode(BillingClient.BillingResponseCode.OK)
                .build() to listOf(mockk {
                every { accountIdentifiers } returns mockk {
                    every { obfuscatedAccountId } returns "" // empty clientId
                }
                every { products } returns listOf(googleProductId)
                every { purchaseState } returns Purchase.PurchaseState.PURCHASED
            })
        )

        // THEN
        assertEquals(
            BillingIAPViewModel.State.Error.ProductPurchaseError.IncorrectCustomerId,
            tested.billingIAPState.value
        )

        val dataSlot = slot<CheckoutGiapBillingPurchaseTotal>()
        verify { observabilityManager.enqueue(capture(dataSlot), any()) }
        assertEquals(
            CheckoutGiapBillingPurchaseTotal.PurchaseStatus.incorrectCustomerId,
            dataSlot.captured.Labels.status
        )
    }
}
