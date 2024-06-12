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
    private val workManager: WorkManager,
) {
    fun start() {
        onPurchased { purchase ->
            addHumanVerificationToken(purchase)
            handlePurchased(purchase)
        }
        onSubscribed {
            clearHumanVerificationToken()
        }
    }

    private suspend fun addHumanVerificationToken(purchase: Purchase) = runCatching {
        val token = requireNotNull(purchase.paymentToken)
        val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
        humanVerificationManager.addDetails(token.humanVerificationDetails(clientId))
    }.onFailure {
        CoreLogger.e(LogTag.PURCHASE_ERROR, it)
    }

    private suspend fun clearHumanVerificationToken() = runCatching {
        val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
        humanVerificationManager.clearDetails(clientId)
    }.onFailure {
        CoreLogger.e(LogTag.PURCHASE_ERROR, it)
    }

    private suspend fun PurchaseStateHandler.handlePurchased(purchase: Purchase) {
        val userId = sessionProvider.getUserId(purchase.sessionId)
        val user = userId?.let { userManager.getUser(userId) }
        when (user?.type) {
            Type.Proton -> subscribePurchase(purchase)
            Type.Managed -> subscribePurchase(purchase)
            Type.External -> subscribePurchase(purchase)
            Type.CredentialLess -> accountWorkflowHandler.handleCreateAccountNeeded(user.userId)
            null -> Unit // No User + SignUp flow.
        }
    }

    private fun subscribePurchase(purchase: Purchase) {
        workManager.enqueueUniqueWork(
            SubscribePurchaseWorker.getOneTimeUniqueWorkName(purchase.planName),
            ExistingWorkPolicy.REPLACE,
            SubscribePurchaseWorker.getRequest(purchase.planName)
        )
    }
}

fun PurchaseStateHandler.onPurchased(
    planName: String? = null,
    block: suspend (Purchase) -> Unit
): Job = purchaseManager
    .onPurchaseState(PurchaseState.Purchased, planName = planName, initialState = true)
    .onEach { purchase -> block(purchase) }
    .catch { CoreLogger.e(LogTag.PURCHASE_ERROR, it) }
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)

fun PurchaseStateHandler.onSubscribed(
    planName: String? = null,
    block: suspend (Purchase) -> Unit
): Job = purchaseManager
    .onPurchaseState(PurchaseState.Subscribed, planName = planName, initialState = true)
    .onEach { purchase -> block(purchase) }
    .catch { CoreLogger.e(LogTag.PURCHASE_ERROR, it) }
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)
