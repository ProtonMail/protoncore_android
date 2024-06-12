/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.paymentiap.data.worker

import android.app.Activity
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import me.proton.core.observability.domain.ObservabilityContext
import me.proton.core.observability.domain.ObservabilityManager
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingAcknowledgeTotal
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.extension.findGooglePurchase
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.paymentiap.domain.LogTag
import me.proton.core.paymentiap.domain.isRetryable
import me.proton.core.paymentiap.domain.toGiapStatus
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.coroutine.withResultContext
import javax.inject.Provider

@HiltWorker
internal class AcknowledgePurchaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val purchaseRepository: PurchaseRepository,
    private val googleBillingRepository: Provider<GoogleBillingRepository<Activity>>,
    override val observabilityManager: ObservabilityManager
) : CoroutineWorker(context, params), ObservabilityContext {

    override suspend fun doWork(): Result = withResultContext {
        onResultEnqueueObservability("acknowledgePurchase") {
            CheckoutGiapBillingAcknowledgeTotal(toGiapStatus())
        }

        val planName = requireNotNull(inputData.getString(INPUT_PLAN_NAME))
        val purchase = requireNotNull(purchaseRepository.getPurchase(planName))
        runCatching {
            require(purchase.paymentProvider == PaymentProvider.GoogleInAppPurchase)
            googleBillingRepository.get().use {
                val googlePurchase = it.findGooglePurchase(purchase)
                checkNotNull(googlePurchase) { "Cannot find google purchase: $purchase" }
                check(!googlePurchase.isAcknowledged) { "Google Purchase already acknowledged: $purchase" }
                it.acknowledgePurchase(googlePurchase.purchaseToken)
             }
        }.fold(
            onSuccess = { onSuccess(purchase) },
            onFailure = { onFailure(purchase, it)}
        )
    }

    private suspend fun onSuccess(purchase: Purchase): Result {
        CoreLogger.w(LogTag.GIAP_INFO,"$TAG, acknowledged: $purchase")
        purchaseRepository.upsertPurchase(purchase.copy(purchaseState = PurchaseState.Acknowledged))
        return Result.success()
    }

    private suspend fun onFailure(purchase: Purchase, error: Throwable): Result {
        return when {
            error is CancellationException -> {
                CoreLogger.w(LogTag.GIAP_INFO, error, "$TAG, retrying: $purchase")
                Result.retry()
            }
            error is BillingClientError && error.isRetryable() -> {
                CoreLogger.w(LogTag.GIAP_INFO, error, "$TAG, retrying: $purchase")
                Result.retry()
            }
            else -> {
                CoreLogger.e(LogTag.GIAP_ERROR, error, "$TAG, failed: $purchase")
                purchaseRepository.upsertPurchase(
                    purchase.copy(
                        purchaseFailure = error.localizedMessage,
                        purchaseState = PurchaseState.Failed
                    )
                )
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "AcknowledgePurchaseWorker"
        private const val INPUT_PLAN_NAME = "arg.planName"

        fun getOneTimeUniqueWorkName(planName: String) = "$TAG-$planName"

        fun getRequest(planName: String): OneTimeWorkRequest {
            val inputData = workDataOf(INPUT_PLAN_NAME to planName)
            return OneTimeWorkRequestBuilder<AcknowledgePurchaseWorker>()
                .setInputData(inputData)
                .build()
        }
    }
}
