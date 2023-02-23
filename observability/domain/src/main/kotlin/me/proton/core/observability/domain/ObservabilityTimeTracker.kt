package me.proton.core.observability.domain

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Singleton
public class ObservabilityTimeTracker constructor(
    private val clockMillis: () -> Long,
) {
    private val firstEnqueuedEventAtMs = MutexValue<Long?>(null)

    internal suspend fun clear() = firstEnqueuedEventAtMs.setValue(null)

    internal suspend fun getDurationSinceFirstEvent(): Duration? =
        firstEnqueuedEventAtMs.getValue()?.let { clockMillis() - it }?.milliseconds

    internal suspend fun setFirstEventNow() {
        firstEnqueuedEventAtMs.setValue(clockMillis())
    }

    private class MutexValue<T>(initialValue: T) {
        private val mutex = Mutex()
        private var value: T = initialValue

        suspend fun getValue(): T = mutex.withLock { value }
        suspend fun setValue(newValue: T) = mutex.withLock { value = newValue }
    }
}
