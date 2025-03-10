/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.GoogleServicesAvailability
import me.proton.core.payment.domain.usecase.GoogleServicesUtils
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.plan.domain.SupportUpgradePaidPlans
import me.proton.core.plan.domain.entity.SubscriptionManagement
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.canReadSubscription
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

class CanUpgradeFromMobile @Inject constructor(
    @SupportUpgradePaidPlans val supportPaidPlans: Boolean,
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders,
    private val getCurrentSubscription: GetDynamicSubscription,
    private val googleServicesUtils: Optional<GoogleServicesUtils>,
    private val userManager: UserManager
) {

    suspend operator fun invoke(userId: UserId): Boolean {
        if (!supportPaidPlans) {
            return false
        }
        if (!userManager.getUser(userId).canReadSubscription()) {
            return false
        }
        val subscription = getCurrentSubscription(userId)
        if (subscription.external != null && subscription.external != SubscriptionManagement.GOOGLE_MANAGED) {
            return false
        }
        val paymentProviders = getAvailablePaymentProviders().filter {
            when (it) {
                PaymentProvider.GoogleInAppPurchase -> hasGooglePlayServices()
                PaymentProvider.PayPal -> false // It's not possible to setup PayPal during signup, from mobile app.
                else -> true
            }
        }
        return paymentProviders.isNotEmpty()
    }

    private fun hasGooglePlayServices(): Boolean =
        googleServicesUtils.getOrNull()?.isGooglePlayServicesAvailable() == GoogleServicesAvailability.Success
}
