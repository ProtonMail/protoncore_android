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

package me.proton.core.plan.data

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.entity.humanVerificationDetails
import me.proton.core.payment.domain.onPurchaseState
import me.proton.core.plan.data.worker.CheckProtonTokenApprovalWorker
import me.proton.core.plan.data.worker.CreateProtonTokenWorker
import me.proton.core.plan.data.worker.SubscribePurchaseWorker
import me.proton.core.plan.domain.LogTag
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.Type
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseStateHandler @Inject constructor(
    internal val scopeProvider: CoroutineScopeProvider,
    internal val purchaseManager: PurchaseManager,
    internal val userManager: UserManager,
    internal val sessionProvider: SessionProvider,
    internal val clientIdProvider: ClientIdProvider,
    internal val humanVerificationManager: HumanVerificationManager,
    private val accountWorkflowHandler: AccountWorkflowHandler,
    private val workManager: WorkManager
) {

    fun start() {
        onPurchased { purchase ->
            handlePurchased(purchase)
        }
        onTokenized { purchase ->
            addHumanVerificationToken(purchase)
            enqueuePurchaseApproval(purchase)
        }
        onApproved { purchase ->
            enqueuePurchaseSubscription(purchase)
        }
        onSubscribed {
            clearHumanVerificationToken()
        }
    }

    //region Purchased

    private suspend fun addHumanVerificationToken(purchase: Purchase) {
        runCatching {
            val token = requireNotNull(purchase.paymentToken)
            val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
            humanVerificationManager.addDetails(token.humanVerificationDetails(clientId))
        }.onFailure { error ->
            CoreLogger.e(LogTag.PURCHASE_ERROR, error)
        }
    }

    private suspend fun handlePurchased(purchase: Purchase) {
        sessionProvider.getUserId(purchase.sessionId)?.let { sessionUserId ->
            val user = userManager.getUser(sessionUserId)

            when (user.type) {
                Type.Proton,
                Type.Managed,
                Type.External -> {
                    enqueuePurchaseTokenization(purchase)
                }
                Type.CredentialLess -> {
                    accountWorkflowHandler.handleCreateAccountNeeded(sessionUserId)
                }
                else -> Unit
            }
        }
    }

    private fun enqueuePurchaseTokenization(purchase: Purchase) {
        workManager.enqueueUniqueWork(
            CreateProtonTokenWorker.getOneTimeUniqueWorkName(purchase.planName),
            ExistingWorkPolicy.REPLACE,
            CreateProtonTokenWorker.getRequest(purchase.planName)
        )
    }

    //endregion

    //region Tokenized

    private fun enqueuePurchaseApproval(purchase: Purchase) {
        workManager.enqueueUniqueWork(
            CheckProtonTokenApprovalWorker.getOneTimeUniqueWorkName(purchase.planName),
            ExistingWorkPolicy.REPLACE,
            CheckProtonTokenApprovalWorker.getRequest(purchase.planName)
        )
    }

    //endregion

    //region Approved

    private fun enqueuePurchaseSubscription(purchase: Purchase) {
        workManager.enqueueUniqueWork(
            SubscribePurchaseWorker.getOneTimeUniqueWorkName(purchase.planName),
            ExistingWorkPolicy.REPLACE,
            SubscribePurchaseWorker.getRequest(purchase.planName)
        )
    }

    //endregion

    //region Subscribed

    private suspend fun clearHumanVerificationToken() {
        runCatching {
            val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
            humanVerificationManager.clearDetails(clientId)
        }.onFailure { error ->
            CoreLogger.e(LogTag.PURCHASE_ERROR, error)
        }
    }

    //endregion
}

//region State observation extensions

fun PurchaseStateHandler.onPurchased(
    planName: String? = null,
    block: suspend (Purchase) -> Unit
): Job = purchaseManager
    .onPurchaseState(PurchaseState.Purchased, planName = planName, initialState = true)
    .onEach { purchase -> block(purchase) }
    .catch { CoreLogger.e(LogTag.PURCHASE_ERROR, it) }
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)

fun PurchaseStateHandler.onTokenized(
    planName: String? = null,
    block: suspend (Purchase) -> Unit
): Job {
    return purchaseManager
        .onPurchaseState(PurchaseState.Tokenized, planName = planName, initialState = true)
        .onEach { purchase -> block(purchase) }
        .catch { error -> CoreLogger.e(LogTag.PURCHASE_ERROR, error) }
        .launchIn(scopeProvider.GlobalDefaultSupervisedScope)
}

fun PurchaseStateHandler.onApproved(
    planName: String? = null,
    block: suspend (Purchase) -> Unit
): Job {
    return purchaseManager
        .onPurchaseState(PurchaseState.Approved, planName = planName, initialState = true)
        .onEach { purchase -> block(purchase) }
        .catch { error -> CoreLogger.e(LogTag.PURCHASE_ERROR, error) }
        .launchIn(scopeProvider.GlobalDefaultSupervisedScope)
}

fun PurchaseStateHandler.onSubscribed(
    planName: String? = null,
    block: suspend (Purchase) -> Unit
): Job = purchaseManager
    .onPurchaseState(PurchaseState.Subscribed, planName = planName, initialState = true)
    .onEach { purchase -> block(purchase) }
    .catch { CoreLogger.e(LogTag.PURCHASE_ERROR, it) }
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)

//endregion