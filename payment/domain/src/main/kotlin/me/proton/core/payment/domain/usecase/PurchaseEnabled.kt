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

import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.FeatureId
import me.proton.core.util.kotlin.CoreLogger
import java.io.IOException
import javax.inject.Inject

class PurchaseEnabled @Inject constructor(
    private val featureFlagManager: FeatureFlagManager
) {
    suspend operator fun invoke(): Boolean {
        val isEnabled = try {
            val paymentsFeatureFlag = featureFlagManager.get(
                userId = null,
                featureId = FeatureId(PAYMENTS_ANDROID_FEATURE_FLAG),
                refresh = true
            )?.value
            paymentsFeatureFlag == false
        } catch (exception: IOException) {
            // if the flag does not exists at all on the api, we assume that the payments are default enabled
            CoreLogger.e(LogTag.DEFAULT, exception)
            true
        }

        return isEnabled
    }

    companion object {
        internal const val PAYMENTS_ANDROID_FEATURE_FLAG = "PaymentsAndroidDisabled"

        object LogTag {
            const val DEFAULT = "core.payments.default"
        }
    }
}
