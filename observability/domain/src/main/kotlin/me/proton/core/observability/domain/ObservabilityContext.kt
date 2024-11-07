package me.proton.core.observability.domain

import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.util.kotlin.coroutine.ResultCollector
import me.proton.core.util.kotlin.coroutine.onFailure
import me.proton.core.util.kotlin.coroutine.onSuccess

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

    public suspend fun <T> ResultCollector<T>.onFailureEnqueueObservability(
        block: Result<T>.() -> ObservabilityData
    ): Unit = onFailure { enqueueObservability(block) }

    public suspend fun <T> ResultCollector<T>.onSuccessEnqueueObservability(
        block: Result<T>.() -> ObservabilityData
    ): Unit = onSuccess { enqueueObservability(block) }
}
