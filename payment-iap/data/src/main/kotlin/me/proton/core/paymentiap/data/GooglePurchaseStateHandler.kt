package me.proton.core.paymentiap.data

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.onPurchaseState
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.paymentiap.data.worker.AcknowledgePurchaseWorker
import me.proton.core.paymentiap.data.worker.DeletePurchaseWorker
import me.proton.core.paymentiap.domain.LogTag
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class GooglePurchaseStateHandler @Inject constructor(
    internal val scopeProvider: CoroutineScopeProvider,
    internal val purchaseManager: PurchaseManager,
    private val workManager: WorkManager
) {

    public fun start() {
        onGiapSubscribed { purchase ->
            acknowledgePurchase(purchase)
        }

        onGiapAcknowledged { purchase ->
            deletePurchase(purchase)
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        workManager.enqueueUniqueWork(
            AcknowledgePurchaseWorker.getOneTimeUniqueWorkName(purchase.planName),
            ExistingWorkPolicy.REPLACE,
            AcknowledgePurchaseWorker.getRequest(purchase.planName)
        )
    }

    private fun deletePurchase(purchase: Purchase) {
        workManager.enqueueUniqueWork(
            DeletePurchaseWorker.getOneTimeUniqueWorkName(purchase.planName),
            ExistingWorkPolicy.REPLACE,
            DeletePurchaseWorker.getRequest(purchase.planName)
        )
    }
}

public fun GooglePurchaseStateHandler.onGiapSubscribed(
    planName: String? = null,
    block: suspend (Purchase) -> Unit
): Job = purchaseManager
    .onPurchaseState(PurchaseState.Subscribed, planName = planName, initialState = true)
    .filter { it.paymentProvider == PaymentProvider.GoogleInAppPurchase }
    .onEach { purchase -> block(purchase) }
    .catch { CoreLogger.e(LogTag.GIAP_ERROR, it) }
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)

public fun GooglePurchaseStateHandler.onGiapAcknowledged(
    planName: String? = null,
    block: suspend (Purchase) -> Unit
): Job = purchaseManager
    .onPurchaseState(PurchaseState.Acknowledged, planName = planName, initialState = true)
    .filter { it.paymentProvider == PaymentProvider.GoogleInAppPurchase }
    .onEach { purchase -> block(purchase) }
    .catch { CoreLogger.e(LogTag.GIAP_ERROR, it) }
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)
