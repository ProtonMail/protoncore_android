/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.payment.domain.usecase

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.entity.FeatureId
import javax.inject.Inject

class GetAvailablePaymentProviders @Inject internal constructor(
    private val accountManager: AccountManager,
    private val featureFlagManager: FeatureFlagManager,
    private val googlePlayBillingLibrary: GooglePlayBillingLibrary
) {
    /** Returns a set of [payment providers][PaymentProvider] which can be offered to the user.
     * @throws me.proton.core.network.domain.ApiException
     */
    suspend operator fun invoke(refresh: Boolean = false): Set<PaymentProvider> {
        if (getFeatureFlagValue(AllPaymentsDisabled, refresh)) {
            return emptySet()
        }

        return buildSet {
            if (googlePlayBillingLibrary.isAvailable() && getFeatureFlagValue(GoogleIAPEnabled, refresh)) {
                add(PaymentProvider.GoogleInAppPurchase)
            }

            if (getFeatureFlagValue(ProtonCardPaymentsEnabled, refresh)) {
                add(PaymentProvider.ProtonPayment)
            }
        }
    }

    private suspend fun getFeatureFlagValue(defaultFeatureFlag: FeatureFlag, refresh: Boolean): Boolean {
        val userId = accountManager.getPrimaryUserId().first()
        val featureFlag = featureFlagManager.get(userId, defaultFeatureFlag.featureId, refresh = refresh)
        return featureFlag?.value ?: defaultFeatureFlag.value
    }

    companion object {
        // Note: the flags below define a default value, in case we cannot obtain the actual value from the server.
        internal val AllPaymentsDisabled get() = FeatureFlag(FeatureId("PaymentsAndroidDisabled"), false)
        internal val GoogleIAPEnabled get() = FeatureFlag(FeatureId("EnableAndroidIAP"), false)
        internal val ProtonCardPaymentsEnabled get() = FeatureFlag(FeatureId("EnableAndroidCardPayments"), false)
    }
}

enum class PaymentProvider {
    GoogleInAppPurchase,
    ProtonPayment
}
