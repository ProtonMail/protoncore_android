/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.plan.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.payment.domain.usecase.FindGooglePurchaseForPaymentOrderId
import me.proton.core.plan.domain.LogTag
import me.proton.core.payment.domain.isRecoverable
import me.proton.core.plan.domain.usecase.CreatePaymentTokenForGooglePurchase
import me.proton.core.util.android.workmanager.builder.setExpeditedIfPossible
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.coroutine.withResultContext

//region Constants

private const val TAG: String = "CreateProtonTokenWorker"
private const val INPUT_PLAN_NAME: String = "arg.planName"

//endregion

/**
 * This worker aims to facilitate the creation of a [ProtonPaymentToken]. This token will be
 * created by exchanging a [GooglePurchaseToken].
 *
 * Upon success, the locally persisted [Purchase] will be upserted with the [PurchaseState] of
 * [PurchaseState.Tokenized].
 *
 * If this worker fails to create a token, it will retry until failure. On failure, the purchase
 * will be caught as an unredeemed purchase which will trigger another try of tokenization.
 */
@HiltWorker
internal class CreateProtonTokenWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val createPaymentToken: CreatePaymentTokenForGooglePurchase,
    private val findGooglePurchase: FindGooglePurchaseForPaymentOrderId,
    private val purchaseRepository: PurchaseRepository,
    private val sessionProvider: SessionProvider
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withResultContext {

            val planName = requireNotNull(inputData.getString(INPUT_PLAN_NAME))
            val purchase = requireNotNull(purchaseRepository.getPurchase(planName))

            runCatching {
                val userId = requireNotNull(sessionProvider.getUserId(purchase.sessionId))
                val googlePurchase = requireNotNull(findGooglePurchase(purchase.paymentOrderId))

                createPaymentToken(
                    googleProductId = googlePurchase.productIds.first(),
                    purchase = googlePurchase,
                    userId = userId,
                )
            }.fold(
                onSuccess = { result -> onSuccess(purchase, result) },
                onFailure = { error -> onFailure(purchase, error) }
            )
        }
    }

    private suspend fun onSuccess(
        purchase: Purchase,
        result: CreatePaymentTokenForGooglePurchase.Result
    ): Result {
        CoreLogger.w(LogTag.PURCHASE_INFO, "$TAG, token was created: $purchase")

        purchaseRepository.upsertPurchase(
            purchase.copy(
                purchaseState = PurchaseState.Tokenized,
                paymentToken = result.token
            )
        )

        return Result.success()
    }

    private suspend fun onFailure(purchase: Purchase, error: Throwable): Result {
        return if (error.isRecoverable()) {
            CoreLogger.w(LogTag.PURCHASE_INFO, "$TAG, retrying: $purchase")

            Result.retry()
        } else {
            CoreLogger.e(LogTag.PURCHASE_ERROR, "$TAG, permanent failure: $purchase")

            setPurchaseFailed(purchase, error)
            Result.failure()
        }
    }

    private suspend fun setPurchaseFailed(purchase: Purchase, error: Throwable) {
        purchaseRepository.upsertPurchase(
            purchase.copy(
                purchaseFailure = error.message,
                purchaseState = PurchaseState.Failed
            )
        )
    }

    companion object {

        fun getOneTimeUniqueWorkName(planName: String): String {
            return "$TAG-$planName"
        }

        fun getRequest(planName: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<CreateProtonTokenWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(workDataOf(INPUT_PLAN_NAME to planName))
                .setExpeditedIfPossible()
                .build()
        }
    }
}
