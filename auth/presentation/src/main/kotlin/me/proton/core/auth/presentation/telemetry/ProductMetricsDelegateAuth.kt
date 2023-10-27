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
        dimensions = mutableMapOf<String, String>().apply {
            accountType?.let { put(KEY_ACCOUNT_TYPE, it.name.lowercase()) }
        }
    )

    fun InputValidationResult.toTelemetryEvent(
        name: String
    ): TelemetryEvent = TelemetryEvent(
        name = name,
        group = productGroup,
        dimensions = productDimensions
            .plus(
                ProductMetricsDelegate.KEY_RESULT to when(isValid) {
                true -> ProductMetricsDelegate.VALUE_SUCCESS
                false -> {
                    when (validationType) {
                        ValidationType.PasswordMinLength -> VALUE_PASS_WEAK
                        ValidationType.PasswordMatch -> VALUE_PASS_MISMATCH
                        else -> ProductMetricsDelegate.VALUE_FAILURE
                    }
                }
            })
    )

    companion object {
        const val KEY_ACCOUNT_TYPE: String = "account_type"

        const val VALUE_PASS_MISMATCH: String = "password_mismatch"
        const val VALUE_PASS_WEAK: String = "password_too_weak"
    }
}
