package me.proton.core.plan.test

import me.proton.core.paymentiap.test.robot.PlayStoreHomeRobot

public object SubscriptionHelper {

    public fun cancelSubscription(billingPlan: BillingPlan) {
        PlayStoreHomeRobot()
            .clickOnAccountButton()
            .selectPaymentsAndSubscriptionsItem()
            .selectSubscriptionsItem()
            .clickActiveSubscriptionByText(
                billingPlan.name.takeIf {
                    billingPlan.nameExtension.isEmpty()
                }
                    ?: "${billingPlan.name} ${billingPlan.nameExtension}"
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
