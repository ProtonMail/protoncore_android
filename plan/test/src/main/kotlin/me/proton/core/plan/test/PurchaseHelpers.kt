package me.proton.core.plan.test

import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.onPurchaseState
import me.proton.core.paymentiap.test.robot.PlayStoreHomeRobot

public class PurchaseManagerHandler(private val purchaseManager: PurchaseManager) {
    public fun waitForPurchaseState(
        billingPlan: BillingPlan,
        state: PurchaseState,
        timeMills: Long = 10000
    ) {
        runBlocking {
            withTimeoutOrNull(timeMills) {
                purchaseManager.onPurchaseState(state, planName = billingPlan.id).first()
                Log.d(
                    "GIAP_TEST",
                    "✅ PurchaseManager -> Purchase is in state '$state': " +
                            "${billingPlan.name} - ${billingPlan.id}"
                )
            }
        }
    }
}

public object SubscriptionHelper {

    public fun cancelSubscription(billingPlan: BillingPlan) {
        PlayStoreHomeRobot()
            .clickOnAccountButton()
            .selectPaymentsAndSubscriptionsItem()
            .selectSubscriptionsItem()
            .clickActiveSubscriptionByText(
                billingPlan.name.takeIf {
                    billingPlan.billingCycle.value == BillingCycle.PAY_ANNUALLY
                }
                    ?: "${billingPlan.name} 1 month"
            )
            .clickCancelSubscription()
            .clickNoThanksButton(
                billingPlan.billingCycle.value == BillingCycle.PAY_MONTHLY
            )
            .clickIDontUseServiceEnoughItemAndPressContinue()
            .clickCancelSubscription()
            .subscriptionIsCancelled()
    }
}
