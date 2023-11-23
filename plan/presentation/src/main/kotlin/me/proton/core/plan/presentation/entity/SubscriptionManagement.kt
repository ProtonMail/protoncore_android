package me.proton.core.plan.presentation.entity

import androidx.annotation.StringRes
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.plan.presentation.R

@StringRes
fun SubscriptionManagement?.toStringRes(canUpgradeFromMobile: Boolean): Int? = when {
    !canUpgradeFromMobile -> R.string.plans_can_not_upgrade_from_mobile
    this == SubscriptionManagement.GOOGLE_MANAGED -> R.string.plans_manage_your_subscription_google
    this == SubscriptionManagement.PROTON_MANAGED -> R.string.plans_manage_your_subscription_other
    else -> null
}
