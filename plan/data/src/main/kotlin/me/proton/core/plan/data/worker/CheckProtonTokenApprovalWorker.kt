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
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.util.kotlin.coroutine.withResultContext
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.features.IsOmnichannelEnabled
import me.proton.core.payment.domain.usecase.PollPaymentTokenStatus
import me.proton.core.plan.domain.LogTag
import me.proton.core.util.android.workmanager.builder.setExpeditedIfPossible
import me.proton.core.util.kotlin.CoreLogger

//region Constants

private const val TAG: String = "CheckPaymentTokenApprovalWorker"
private const val INPUT_PLAN_NAME: String = "arg.planName"

//endregion

/**
 * This worker aims to facilitate the polling of an endpoint that checks the current status for a
 * given [ProtonPaymentToken]. When the token is approved (CHARGEABLE), this worker will stop and
 * upsert the contextual [Purchase] with the [PurchaseState] of [PurchaseState.Approved].
 *
 * See the [PollPaymentTokenStatus] use-case for details about it's routine.
 *
 * Note: This worker incorporates the [isOmnichannelEnabled] feature flag. This feature flag will
 * be present in the transitional phase of moving from "V5" payments to "omnichannel" payments. Once
 * we confirm complete stability over payments via omnichannel, we will remove the feature flag,
 * largely due to the fact that "V5" payments will not be possible later.
 */
@HiltWorker
internal class CheckProtonTokenApprovalWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val isOmnichannelEnabled: IsOmnichannelEnabled,
    private val pollPaymentTokenStatus: PollPaymentTokenStatus,
    private val purchaseRepository: PurchaseRepository,
    private val sessionProvider: SessionProvider
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withResultContext {
            val planName = requireNotNull(inputData.getString(INPUT_PLAN_NAME))
            val purchase = requireNotNull(purchaseRepository.getPurchase(planName))

            runCatching {
                val userId = requireNotNull(sessionProvider.getUserId(purchase.sessionId))

                if (isOmnichannelEnabled(userId)) {
                    val paymentToken = requireNotNull(purchase.paymentToken)
                    pollPaymentTokenStatus(userId, paymentToken)
                } else {
                    PaymentTokenResult.PaymentTokenStatusResult(PaymentTokenStatus.CHARGEABLE)
                }
            }.fold(
                onSuccess = { setPurchaseApproved(purchase) },
                onFailure = { error -> setPurchaseFailed(purchase, error) }
            )
        }
    }

    private suspend fun setPurchaseApproved(purchase: Purchase): Result {
        CoreLogger.w(LogTag.DEFAULT, "Proton Payment Token was approved for purchase: $purchase")

        purchaseRepository.upsertPurchase(purchase.copy(purchaseState = PurchaseState.Approved))

        return Result.success()
    }

    /**
     * A later MR will adapt this to account for recoverable errors when a endpoint polling
     * mechanism is in place.
     */
    private suspend fun setPurchaseFailed(purchase: Purchase, error: Throwable): Result {
        CoreLogger.e(LogTag.PURCHASE_ERROR, "$TAG, permanent failure: $purchase")

        purchaseRepository.upsertPurchase(
            purchase.copy(
                purchaseFailure = error.message,
                purchaseState = PurchaseState.Failed
            )
        )

        return Result.failure()
    }

    companion object {

        fun getOneTimeUniqueWorkName(planName: String): String {
            return "$TAG-$planName"
        }

        fun getRequest(planName: String): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<CheckProtonTokenApprovalWorker>()
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