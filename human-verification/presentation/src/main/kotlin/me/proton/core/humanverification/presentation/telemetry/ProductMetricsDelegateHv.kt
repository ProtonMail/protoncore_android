package me.proton.core.humanverification.presentation.telemetry

import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.telemetry.presentation.ProductMetricsDelegate

internal interface ProductMetricsDelegateHv : ProductMetricsDelegate {

    fun toTelemetryHelpEvent(
        name: String
    ): TelemetryEvent = TelemetryEvent(
        name = name,
        group = productGroup,
        dimensions = productDimensions
            .plus(KEY_HV_INTERACTION_EVENT to "help")
    )

    fun toTelemetryEvent(
        name: String,
        interaction: String
    ): TelemetryEvent = TelemetryEvent(
        name = name,
        group = productGroup,
        dimensions = productDimensions
            .plus(KEY_HV_INTERACTION_EVENT to interaction.lowercase())
    )

    fun toTelemetryEvent(
        name: String,
        isSuccess: Boolean
    ): TelemetryEvent =
        TelemetryEvent(
            name = name,
            group = productGroup,
            dimensions = productDimensions
                .plus(ProductMetricsDelegate.KEY_RESULT to if (isSuccess) ProductMetricsDelegate.VALUE_SUCCESS else ProductMetricsDelegate.VALUE_FAILURE),
        )

    companion object {
        const val KEY_HV_INTERACTION_EVENT: String = "interaction_event"
    }
}
