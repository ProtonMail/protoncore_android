package me.proton.core.auth.presentation.telemetry

import me.proton.core.account.domain.entity.AccountType
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

    companion object {
        const val KEY_ACCOUNT_TYPE: String = "account_type"
    }
}
