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

package me.proton.core.auth.presentation.telemetry

import me.proton.core.account.domain.entity.AccountType
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.presentation.ProductMetricsDelegate

internal interface ProductMetricsDelegateAuth : ProductMetricsDelegate {

    fun Result<*>.toTelemetryEvent(
        name: String,
        accountType: AccountType? = null
    ): TelemetryEvent = toTelemetryEvent(
        name = name,
        dimensions = productDimensions.let {
            if (accountType != null) {
                it.plus(KEY_ACCOUNT_TYPE to accountType.name.lowercase())
            } else it
        }
    )

    fun InputValidationResult.toTelemetryEvent(
        name: String
    ): TelemetryEvent = TelemetryEvent(
        name = name,
        group = productGroup,
        dimensions = productDimensions
            .plus(
                ProductMetricsDelegate.KEY_RESULT to when (isValid) {
                    true -> ProductMetricsDelegate.VALUE_SUCCESS
                    false -> {
                        when (validationType) {
                            ValidationType.PasswordMinLength -> VALUE_PASS_WEAK
                            ValidationType.PasswordMatch -> VALUE_PASS_MISMATCH
                            else -> ProductMetricsDelegate.VALUE_FAILURE
                        }
                    }
                }
            )
    )

    companion object {
        const val KEY_ACCOUNT_TYPE: String = "account_type"
        const val KEY_METHOD_TYPE: String = "method_type"

        const val VALUE_PASS_MISMATCH: String = "password_mismatch"
        const val VALUE_PASS_WEAK: String = "password_too_weak"
    }
}
