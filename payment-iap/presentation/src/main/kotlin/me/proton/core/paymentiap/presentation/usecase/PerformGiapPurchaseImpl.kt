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
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.payment.domain.entity.GoogleBillingFlowParams
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.payment.domain.usecase.LaunchGiapBillingFlow
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.payment.domain.usecase.PrepareGiapPurchase
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlanVendor
import me.proton.core.plan.domain.usecase.CreatePaymentTokenForGooglePurchase
import me.proton.core.plan.domain.usecase.PerformGiapPurchase
import me.proton.core.plan.domain.usecase.PerformGiapPurchase.Result
import me.proton.core.plan.domain.usecase.PerformGiapPurchase.Result.Error
import javax.inject.Inject

/**
 * Performs minimal needed GIAP flow:
 * - Prepare GIAP billing.
 * - Launch GIAP popup.
 * - Create payment token from Google token.
 * - Insert new Purchase (next steps handled by PurchaseStateHandler).
 */
public class PerformGiapPurchaseImpl @Inject constructor(
    private val createPaymentTokenForGooglePurchase: CreatePaymentTokenForGooglePurchase,
    private val launchGiapBillingFlow: LaunchGiapBillingFlow<Activity>,
    private val prepareGiapPurchase: PrepareGiapPurchase,
    private val purchaseRepository: PurchaseRepository,
    private val sessionProvider: SessionProvider
) : PerformGiapPurchase<Activity> {
    override suspend fun invoke(
        activity: Activity,
        cycle: Int,
        plan: DynamicPlan,
        userId: UserId?
    ): Result {
        return try {
            val vendorPlanDetails = plan.findGooglePlanDetails(cycle)
            val googleProductId = ProductId(vendorPlanDetails.productId)
            val prepareResult = prepareGiapPurchase(vendorPlanDetails.customerId, googleProductId)
            onPrepareGiapPurchaseResult(
                activity = activity,
                cycle = cycle,
                googleProductId = googleProductId,
                plan = plan,
                prepareResult = prepareResult,
                userId = userId
            )
        } catch (e: BillingClientError) {
            when (e.responseCode) {
                BillingResponseCode.SERVICE_TIMEOUT,
                BillingResponseCode.SERVICE_DISCONNECTED,
                BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingResponseCode.BILLING_UNAVAILABLE,
                BillingResponseCode.ERROR,
                BillingResponseCode.ITEM_ALREADY_OWNED,
                BillingResponseCode.ITEM_NOT_OWNED -> Error.RecoverableBillingError(e)
                BillingResponseCode.USER_CANCELED -> Error.UserCancelled
                else -> Error.UnrecoverableBillingError(e)
            }
        }
    }

    @Suppress("LongParameterList")
    private suspend fun onPrepareGiapPurchaseResult(
        activity: Activity,
        cycle: Int,
        googleProductId: ProductId,
        plan: DynamicPlan,
        prepareResult: PrepareGiapPurchase.Result,
        userId: UserId?
    ): Result = when (prepareResult) {
        is PrepareGiapPurchase.Result.ProductDetailsNotFound -> Error.GoogleProductDetailsNotFound
        is PrepareGiapPurchase.Result.Unredeemed -> Error.GiapUnredeemed(
            cycle = cycle,
            googleProductId = googleProductId,
            googlePurchase = prepareResult.googlePurchase,
            plan = plan
        )
        is PrepareGiapPurchase.Result.Success -> onPrepareGiapPurchaseSuccess(
            activity = activity,
            cycle = cycle,
            googleProductId = googleProductId,
            params = prepareResult.params,
            plan = plan,
            userId = userId
        )
    }

    @Suppress("LongParameterList")
    private suspend fun onPrepareGiapPurchaseSuccess(
        activity: Activity,
        cycle: Int,
        googleProductId: ProductId,
        params: GoogleBillingFlowParams,
        plan: DynamicPlan,
        userId: UserId?
    ): Result {
        val launchResult = launchGiapBillingFlow(activity, googleProductId, params)
        return onGiapBillingResult(cycle, googleProductId, launchResult, plan, userId)
    }

    private suspend fun onGiapBillingResult(
        cycle: Int,
        googleProductId: ProductId,
        launchResult: LaunchGiapBillingFlow.Result,
        plan: DynamicPlan,
        userId: UserId?
    ): Result = when (launchResult) {
        is LaunchGiapBillingFlow.Result.Error.EmptyCustomerId -> Error.EmptyCustomerId
        is LaunchGiapBillingFlow.Result.Error.PurchaseNotFound -> Error.PurchaseNotFound
        is LaunchGiapBillingFlow.Result.PurchaseSuccess -> onLaunchGiapBillingSuccess(
            cycle = cycle,
            googleProductId = googleProductId,
            plan = plan,
            purchase = launchResult.purchase,
            userId = userId
        )
    }

    private suspend fun onLaunchGiapBillingSuccess(
        cycle: Int,
        googleProductId: ProductId,
        plan: DynamicPlan,
        purchase: GooglePurchase,
        userId: UserId?
    ): Result {
        val createTokenResult = createPaymentTokenForGooglePurchase(
            cycle = cycle,
            googleProductId = googleProductId,
            plan = plan,
            purchase = purchase,
            userId = userId
        )

        purchaseRepository.upsertPurchase(
            Purchase(
                sessionId = requireNotNull(sessionProvider.getSessionId(userId)),
                planName = requireNotNull(plan.name),
                planCycle = cycle,
                purchaseState = PurchaseState.Purchased,
                purchaseFailure = null,
                paymentProvider = PaymentProvider.GoogleInAppPurchase,
                paymentOrderId = purchase.orderId,
                paymentToken = createTokenResult.token,
                paymentCurrency = createTokenResult.currency,
                paymentAmount = createTokenResult.amount
            )
        )

        return Result.GiapSuccess(
            purchase = purchase,
            amount = createTokenResult.amount,
            currency = createTokenResult.currency.name,
            token = createTokenResult.token
        )
    }

    private fun DynamicPlan.findGooglePlanDetails(cycle: Int): DynamicPlanVendor =
        requireNotNull(instances[cycle]?.vendors?.get(AppStore.GooglePlay)) {
            "Missing vendor details for ${AppStore.GooglePlay}."
        }
}
