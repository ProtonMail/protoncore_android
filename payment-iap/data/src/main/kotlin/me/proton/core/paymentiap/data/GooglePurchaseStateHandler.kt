package me.proton.core.paymentiap.data

import android.app.Activity
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.extension.findGooglePurchase
import me.proton.core.payment.domain.onPurchaseState
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.paymentiap.data.worker.AcknowledgePurchaseWorker
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
public class GooglePurchaseStateHandler @Inject constructor(
    internal val scopeProvider: CoroutineScopeProvider,
    internal val purchaseManager: PurchaseManager,
    private val googlePurchaseRepository: GooglePurchaseRepository,
    private val googleBillingRepository: Provider<GoogleBillingRepository<Activity>>,
    private val workManager: WorkManager
) {
    private suspend fun findGooglePurchase(purchase: Purchase) =
        googleBillingRepository.get().use { it.findGooglePurchase(purchase) }

    public fun start() {
        onGiapSubscribed { purchase ->
            findGooglePurchase(purchase)?.takeUnless { it.isAcknowledged }?.let {
                acknowledgePurchase(purchase)
            }
        }

        onGiapAcknowledged { purchase ->
            findGooglePurchase(purchase)?.let {
                googlePurchaseRepository.deleteByGooglePurchaseToken(it.purchaseToken)
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        workManager.enqueueUniqueWork(
            AcknowledgePurchaseWorker.getOneTimeUniqueWorkName(purchase.planName),
            ExistingWorkPolicy.REPLACE,
            AcknowledgePurchaseWorker.getRequest(purchase.planName)
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
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)

public fun GooglePurchaseStateHandler.onGiapAcknowledged(
    planName: String? = null,
    block: suspend (Purchase) -> Unit
): Job = purchaseManager
    .onPurchaseState(PurchaseState.Acknowledged, planName = planName, initialState = true)
    .filter { it.paymentProvider == PaymentProvider.GoogleInAppPurchase }
    .onEach { purchase -> block(purchase) }
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)
