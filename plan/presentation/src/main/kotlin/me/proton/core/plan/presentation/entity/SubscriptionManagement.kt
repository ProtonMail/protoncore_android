package me.proton.core.plan.presentation.entity

import androidx.annotation.StringRes
import me.proton.core.payment.domain.entity.SubscriptionManagement
import me.proton.core.plan.presentation.R

@StringRes
fun SubscriptionManagement?.toStringRes(): Int = when (this) {
    SubscriptionManagement.GOOGLE_MANAGED -> R.string.plans_manage_your_subscription_google
    else -> R.string.plans_manage_your_subscription_other
}
