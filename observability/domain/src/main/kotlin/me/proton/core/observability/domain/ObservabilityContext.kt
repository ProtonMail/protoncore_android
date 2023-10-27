package me.proton.core.observability.domain

import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.util.kotlin.coroutine.ResultCollector

public interface ObservabilityContext {

    public val observabilityManager: ObservabilityManager

    public fun enqueueObservability(data: ObservabilityData): Unit = observabilityManager.enqueue(data)

    public fun <T> Result<T>.enqueueObservability(
        block: Result<T>.() -> ObservabilityData
    ): Result<T> = also { enqueueObservability(block(this)) }

    public suspend fun <T> ResultCollector<T>.onResultEnqueueObservability(
        key: String,
        block: Result<T>.() -> ObservabilityData
    ): Unit = onResult(key) { enqueueObservability(block) }

    public suspend fun <T> ResultCollector<T>.onCompleteEnqueueObservability(
        block: Result<T>.() -> ObservabilityData
    ): Unit = onComplete { enqueueObservability(block) }
}
