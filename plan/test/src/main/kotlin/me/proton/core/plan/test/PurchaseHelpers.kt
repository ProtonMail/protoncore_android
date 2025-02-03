package me.proton.core.plan.test

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.paymentiap.data.GooglePurchaseStateHandler
import me.proton.core.paymentiap.data.onGiapAcknowledged
import me.proton.core.paymentiap.data.onGiapSubscribed
import me.proton.core.paymentiap.test.robot.PlayStoreHomeRobot
import me.proton.core.plan.test.robot.BillingCycle
import me.proton.core.plan.test.robot.Plan

public class GiapHandler(private val giapHandler: GooglePurchaseStateHandler) {

    public fun waitForGiapSubscribed(plan: Plan) {
        runBlocking {
            repeat(10) {
                val job = giapHandler.onGiapSubscribed(plan.id) {
                    Log.d(
                        "GIAP_TEST",
                        "✅ GIAP -> Subscription subscribed: ${plan.name} - ${plan.id}"
                    )
                }
                if (job.isCompleted) {
                    return@repeat
                }
                delay(1000)
            }
        }
    }

    public fun waitForGiapAcknowledged(plan: Plan) {
        runBlocking {
            repeat(10) {
                val job = giapHandler.onGiapAcknowledged(plan.id)
                {
                    Log.d(
                        "GIAP_TEST",
                        "✅ GIAP -> Subscription acknowledged: ${plan.name} - ${plan.id}"
                    )
                }
                if (job.isCompleted) {
                    return@repeat
                }
                delay(1000)
            }
        }
    }
}

public class PurchaseManagerHandler(private val purchaseManager: PurchaseManager) {
    public suspend fun waitForPurchaseState(plan: Plan, state: PurchaseState): Purchase? {
        repeat(10) {
            val purchase = purchaseManager
                .observePurchase(plan.id)
                .filter { it?.purchaseState == state }
                .firstOrNull()
            if (purchase != null) {
                Log.d(
                    "GIAP_TEST",
                    "✅ PurchaseManager -> Purchase deleted from database: ${plan.name} - ${plan.id}"
                )
                return purchase
            }
            delay(1000)
        }
        return null
    }
}

public object SubscriptionHelper {

    public fun cancelSubscription(plan: Plan) {
        PlayStoreHomeRobot()
            .clickOnAccountButton()
            .selectPaymentsAndSubscriptionsItem()
            .selectSubscriptionsItem()
            .clickActiveSubscriptionByText(
                plan.name.takeIf { plan.billingCycle.value == BillingCycle.PAY_ANNUALLY }
                    ?: "${plan.name} 1 month"
            )
            .clickCancelSubscription()
            .clickNoThanksButton(plan.billingCycle.value == BillingCycle.PAY_MONTHLY)
            .clickIDontUseServiceEnoughItemAndPressContinue()
            .clickCancelSubscription()
            .subscriptionIsCancelled()
    }
}
