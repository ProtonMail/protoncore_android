package me.proton.core.observability.domain.metrics

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy

/** The base class for any metrics defined in Core repository. */
@Serializable
public sealed class CoreObservabilityData : ObservabilityData() {
    @Suppress("UNCHECKED_CAST")
    override val dataSerializer: SerializationStrategy<ObservabilityData>
        get() = serializer() as SerializationStrategy<ObservabilityData>
}
