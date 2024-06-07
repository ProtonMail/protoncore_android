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
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.extension.findGooglePurchase
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.paymentiap.domain.LogTag
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Provider

@HiltWorker
internal class DeletePurchaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val purchaseRepository: PurchaseRepository,
    private val googlePurchaseRepository: GooglePurchaseRepository,
    private val googleBillingRepository: Provider<GoogleBillingRepository<Activity>>
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val planName = requireNotNull(inputData.getString(INPUT_PLAN_NAME))
        val purchase = requireNotNull(purchaseRepository.getPurchase(planName))
        return runCatching {
            require(purchase.paymentProvider == PaymentProvider.GoogleInAppPurchase)
            googleBillingRepository.get().use {
                val googlePurchase = it.findGooglePurchase(purchase)
                checkNotNull(googlePurchase) { "Cannot find google purchase: $purchase" }
                googlePurchaseRepository.deleteByGooglePurchaseToken(googlePurchase.purchaseToken)
             }
        }.fold(
            onSuccess = {
                CoreLogger.w(LogTag.GIAP_INFO,"$TAG, deleted: $purchase")
                purchaseRepository.upsertPurchase(purchase.copy(purchaseState = PurchaseState.Deleted))
                Result.success()
            },
            onFailure = {
                CoreLogger.e(LogTag.GIAP_ERROR, it, "$TAG, failed: $purchase")
                purchaseRepository.upsertPurchase(
                    purchase.copy(
                        purchaseFailure = it.localizedMessage,
                        purchaseState = PurchaseState.Failed
                    )
                )
                Result.failure()
            }
        )
    }

    companion object {
        private const val TAG = "DeletePurchaseWorker"
        private const val INPUT_PLAN_NAME = "arg.planName"

        fun getOneTimeUniqueWorkName(planName: String) = "$TAG-$planName"

        fun getRequest(planName: String): OneTimeWorkRequest {
            val inputData = workDataOf(INPUT_PLAN_NAME to planName)
            return OneTimeWorkRequestBuilder<DeletePurchaseWorker>()
                .setInputData(inputData)
                .build()
        }
    }
}
