package me.proton.core.observability.domain

import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.util.kotlin.coroutine.ResultCollector

public interface ObservabilityContext {

    public val manager: ObservabilityManager

    public fun enqueue(data: ObservabilityData): Unit = manager.enqueue(data)

    public fun <T> Result<T>.enqueue(
        block: Result<T>.() -> ObservabilityData
    ): Result<T> = also { enqueue(block(this)) }

    public suspend fun <T> ResultCollector<T>.onResultEnqueue(
        key: String,
        block: Result<T>.() -> ObservabilityData
    ): Unit = onResult(key) { enqueue(block) }

    public suspend fun <T> ResultCollector<T>.onCompleteEnqueue(
        block: Result<T>.() -> ObservabilityData
    ): Unit = onComplete { enqueue(block) }
}
