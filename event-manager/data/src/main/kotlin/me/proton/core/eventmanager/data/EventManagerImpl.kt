/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.eventmanager.data

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.eventmanager.data.db.EventMetadataDatabase
import me.proton.core.eventmanager.data.extension.isFetchAllowed
import me.proton.core.eventmanager.data.extension.runCatching
import me.proton.core.eventmanager.data.extension.runInTransaction
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManager
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.LogTag
import me.proton.core.eventmanager.domain.entity.EventId
import me.proton.core.eventmanager.domain.entity.EventMetadata
import me.proton.core.eventmanager.domain.entity.EventsResponse
import me.proton.core.eventmanager.domain.entity.RefreshType
import me.proton.core.eventmanager.domain.entity.State
import me.proton.core.eventmanager.domain.repository.EventMetadataRepository
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isForceUpdate
import me.proton.core.network.domain.isRetryable
import me.proton.core.network.domain.isUnauthorized
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.util.android.datetime.Clock
import me.proton.core.util.android.datetime.UtcClock
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.exhaustive

@AssistedFactory
interface EventManagerFactory {
    fun create(deserializer: EventDeserializer): EventManagerImpl
}

@Suppress("UseIfInsteadOfWhen", "ClassOrdering", "ComplexMethod", "TooManyFunctions")
class EventManagerImpl @AssistedInject constructor(
    private val scopeProvider: CoroutineScopeProvider,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val accountManager: AccountManager,
    private val eventWorkerManager: EventWorkerManager,
    private val database: EventMetadataDatabase,
    internal val eventMetadataRepository: EventMetadataRepository,
    @Assisted val deserializer: EventDeserializer,
    @UtcClock private val clock: Clock
) : EventManager {

    private val lock = Mutex()

    private var observeJob: Job? = null

    internal val eventListenersByOrder = sortedMapOf<Int, MutableSet<EventListener<*, *>>>()

    private suspend fun getMetadataFirstUncompleted() = eventMetadataRepository.get(config)
        .firstOrNull { it.state != State.Completed }

    private suspend fun getMetadataLastCompleted() = eventMetadataRepository.get(config)
        .lastOrNull { it.state == State.Completed }

    private suspend fun processFirstUncompleted() {
        val metadata = getMetadataFirstUncompleted()
        when {
            metadata == null -> stop()
            metadata.retry > retriesBeforeDeleteAllMetadata -> {
                reportFailure(metadata)
                deleteAllMetadata()
            }
            metadata.retry > retriesBeforeNotifyResetAll -> {
                reportFailure(metadata)
                notifyResetAll(metadata)
            }
            metadata.retry > retriesBeforeNotifyFailure -> {
                reportFailure(metadata)
                notifyFailure(metadata)
            }
            else -> when (metadata.state) {
                State.Cancelled -> fetch(metadata)
                State.Enqueued -> fetch(metadata)
                State.Fetching -> fetch(metadata)
                State.Persisted -> notify(metadata)
                State.NotifyResetAll -> notifyResetAll(metadata)
                State.NotifyPrepare -> notifyPrepare(metadata)
                State.NotifyEvents -> notifyPrepare(metadata)
                State.Success -> notifySuccess(metadata)
                State.NotifySuccess -> notifySuccess(metadata)
                State.Failure -> notifyFailure(metadata)
                State.NotifyFailure -> notifyFailure(metadata)
                State.NotifyComplete -> notifyComplete(metadata)
                State.Completed -> enqueueOrStop(immediately = true, failure = false)
            }.exhaustive
        }
    }

    private suspend fun reportFailure(metadata: EventMetadata) {
        val list = eventMetadataRepository.get(config)
        CoreLogger.e(LogTag.REPORT_MAX_RETRY, "Max Failure reached (current: ${metadata.eventId}): $list")
    }

    private suspend fun deleteAllMetadata() {
        eventMetadataRepository.deleteAll(config)
        enqueueOrStop(immediately = true, failure = false)
    }

    private suspend fun fetch(metadata: EventMetadata) {
        CoreLogger.i(LogTag.DEFAULT, "EventManager fetch: $config")
        if (config.isFetchAllowed(metadata, clock).not()) {
            val message = buildString {
                append("EventManager fetch skipped: ")
                append("lastFetchedAt=${metadata.fetchedAt}, ")
                append("minimumFetchInterval=${config.minimumFetchInterval.inWholeMilliseconds}ms, ")
            }
            CoreLogger.i(LogTag.DEFAULT, message)
            enqueueOrStop(immediately = false, failure = false)
            return
        }
        val eventId = metadata.eventId ?: runCatching { getLatestEventId() }
            .onFailure {
                processFetchFailure(metadata, it)
            }.getOrThrow()
        eventMetadataRepository.updateEventId(config, metadata.eventId, eventId)
        runCatching(
            config = config,
            eventId = eventId,
            processingState = State.Fetching,
            successState = State.Persisted,
            failureState = State.Enqueued
        ) {
            val response = getEventResponse(eventId)
            val deserializedMetadata = deserializeEventMetadata(eventId, response)
                .copy(state = State.Persisted, fetchedAt = clock.currentEpochMillis())
            eventMetadataRepository.update(deserializedMetadata, response)
            deserializedMetadata
        }.onFailure {
            processFetchFailure(metadata, it)
        }.onSuccess {
            notify(it)
        }
    }

    private suspend fun processFetchFailure(metadata: EventMetadata, throwable: Throwable) {
        notifyFetchError(throwable)
        when {
            // Note: throw it = Use the WorkManager RETRY mechanism (backoff + network constraint).
            throwable is ApiException && throwable.isForceUpdate() -> throw throwable
            throwable is ApiException && throwable.isUnauthorized() -> throw throwable
            throwable is ApiException && throwable.isRetryable().not() -> permanentFetchFailure(metadata, throwable)
            throwable is SerializationException -> permanentFetchFailure(metadata, throwable)
            else -> throw throwable
        }.exhaustive
    }

    private suspend fun permanentFetchFailure(metadata: EventMetadata, error: Throwable) {
        CoreLogger.e(LogTag.FETCH_ERROR, error)
        notifyResetAll(metadata)
    }

    private suspend fun notify(metadata: EventMetadata) {
        when (metadata.refresh) {
            RefreshType.Nothing -> notifyPrepare(metadata)
            RefreshType.All,
            RefreshType.Mail,
            RefreshType.Contact -> notifyResetAll(metadata)
            else -> notifyResetAll(metadata)
        }.exhaustive
    }

    private suspend fun notifyResetAll(metadata: EventMetadata) {
        CoreLogger.i(LogTag.DEFAULT, "EventManager notifyResetAll: $config")
        val eventId = requireNotNull(metadata.eventId)
        runCatching(
            config = config,
            eventId = eventId,
            processingState = State.NotifyResetAll,
            successState = State.NotifyComplete,
            failureState = State.NotifyResetAll
        ) {
            // If needed, get latest remote eventId, before notifyResetAll, so we don't miss any changes.
            val nextEventId = metadata.nextEventId ?: getLatestEventId()
            eventMetadataRepository.updateNextEventId(config, eventId, nextEventId)
            val response = eventMetadataRepository.getEvents(config, eventId)
            // Fully sequential and ordered.
            eventListenersByOrder.values.flatten().forEach {
                it.notifyResetAll(config, metadata, response)
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueueOrStop(immediately = true, failure = true)
        }.onSuccess {
            notifyComplete(metadata)
        }
    }

    private suspend fun notifyPrepare(metadata: EventMetadata) {
        CoreLogger.i(LogTag.DEFAULT, "EventManager notifyPrepare: $config")
        val eventId = requireNotNull(metadata.eventId)
        runCatching(
            config = config,
            eventId = eventId,
            processingState = State.NotifyPrepare,
            successState = State.NotifyEvents,
            failureState = State.NotifyPrepare
        ) {
            val response = eventMetadataRepository.getEvents(config, eventId)
            // Notify prepare for all listeners.
            eventListenersByOrder.values.flatten().forEach { eventListener ->
                eventListener.notifyPrepare(config, metadata, requireNotNull(response))
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueueOrStop(immediately = true, failure = true)
        }.onSuccess {
            notifyEvents(metadata)
        }
    }

    private suspend fun notifyEvents(metadata: EventMetadata) {
        CoreLogger.i(LogTag.DEFAULT, "EventManager notifyEvents: $config")
        val eventId = requireNotNull(metadata.eventId)
        runInTransaction(
            config = config,
            eventId = eventId,
            processingState = State.NotifyEvents,
            successState = State.Success,
            failureState = State.NotifyPrepare
        ) {
            val response = eventMetadataRepository.getEvents(config, eventId)
            // Fully sequential and ordered.
            eventListenersByOrder.values.flatten().forEach { eventListener ->
                eventListener.notifyEvents(config, metadata, requireNotNull(response))
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueueOrStop(immediately = true, failure = true)
        }.onSuccess {
            notifySuccess(metadata)
        }
    }

    private suspend fun notifySuccess(metadata: EventMetadata) {
        CoreLogger.i(LogTag.DEFAULT, "EventManager notifySuccess: $config")
        val eventId = requireNotNull(metadata.eventId)
        runCatching(
            config = config,
            eventId = eventId,
            processingState = State.NotifySuccess,
            successState = State.NotifyComplete,
            failureState = State.Success
        ) {
            val response = eventMetadataRepository.getEvents(config, eventId)
            // Fully sequential and ordered.
            eventListenersByOrder.values.flatten().forEach { eventListener ->
                eventListener.notifySuccess(config, metadata, requireNotNull(response))
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueueOrStop(immediately = true, failure = true)
        }.onSuccess {
            notifyComplete(metadata)
        }
    }

    private suspend fun notifyFailure(metadata: EventMetadata) {
        CoreLogger.i(LogTag.DEFAULT, "EventManager notifyFailure: $config")
        val eventId = requireNotNull(metadata.eventId)
        runCatching(
            config = config,
            eventId = eventId,
            processingState = State.NotifyFailure,
            successState = State.NotifyResetAll,
            failureState = State.Failure
        ) {
            val response = eventMetadataRepository.getEvents(config, eventId)
            // Fully sequential and ordered.
            eventListenersByOrder.values.flatten().forEach { eventListener ->
                eventListener.notifyFailure(config, metadata, response)
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueueOrStop(immediately = true, failure = true)
        }.onSuccess {
            notifyResetAll(metadata)
        }
    }

    private suspend fun notifyComplete(metadata: EventMetadata) {
        CoreLogger.i(LogTag.DEFAULT, "EventManager notifyComplete: $config")
        val eventId = requireNotNull(metadata.eventId)
        runCatching(
            config = config,
            eventId = eventId,
            processingState = State.NotifyComplete,
            successState = State.Completed,
            failureState = State.Completed,
        ) {
            val response = eventMetadataRepository.getEvents(config, eventId)
            // Fully sequential and ordered.
            eventListenersByOrder.values.flatten().forEach { eventListener ->
                eventListener.notifyComplete(config, metadata, response)
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueueOrStop(immediately = metadata.more ?: true, failure = true)
        }.onSuccess {
            enqueueOrStop(immediately = metadata.more ?: true, failure = false)
        }
    }

    private suspend fun notifyFetchError(throwable: Throwable) {
        CoreLogger.i(LogTag.DEFAULT, throwable, "EventManager notifyFetchError: $config")
        runCatching {
            eventListenersByOrder.values.flatten().forEach { eventListener ->
                eventListener.onFetchError(config, throwable)
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
        }
    }

    private enum class Action { None, Enqueue, Stop }

    private suspend fun getNextAction(failure: Boolean): Action {
        return database.inTransaction {
            val account = accountManager.getAccount(config.userId).firstOrNull()
            when {
                account == null -> Action.Stop
                account.state != AccountState.Ready -> Action.Stop
                else -> {
                    val lastCompleted = getMetadataLastCompleted()
                    val update = when (val metadata = getMetadataFirstUncompleted()) {
                        null -> lastCompleted?.let { EventMetadata.nextFrom(metadata = lastCompleted) }
                            ?: EventMetadata.newFrom(config = config)
                        else -> when (metadata.state) {
                            // Paused states should not change metadata.
                            State.Cancelled,
                            State.Enqueued -> metadata
                            // Final states should enqueue nextEventId.
                            State.Completed -> EventMetadata.nextFrom(metadata)
                            else -> when {
                                // Any failure should be retried.
                                failure -> metadata.copy(retry = metadata.retry + 1)
                                // Prevent cancelling/re-enqueueing currently running non-failure.
                                eventWorkerManager.isRunning(config) -> null
                                // All other states should be retried.
                                else -> metadata.copy(retry = metadata.retry + 1)
                            }
                        }
                    }
                    when (update) {
                        null -> Action.None
                        else -> runCatching { eventMetadataRepository.updateMetadata(update) }.fold(
                            onSuccess = { Action.Enqueue },
                            onFailure = { Action.None }
                        )
                    }
                }
            }
        }
    }

    private suspend fun enqueueOrStop(immediately: Boolean, failure: Boolean) {
        CoreLogger.i(LogTag.DEFAULT, "EventManager enqueueOrStop ($immediately, $failure): $config")
        when (getNextAction(failure)) {
            Action.None -> Unit
            Action.Enqueue -> eventWorkerManager.enqueue(config, immediately)
            Action.Stop -> stop()
        }
    }

    // Observe Account & App State changes.
    private suspend fun collectStateChanges() {
        combine(
            accountManager.getAccount(config.userId).distinctUntilChangedBy { it?.state },
            appLifecycleProvider.state,
        ) { _, state -> state }
            .onEach { enqueueOrStop(immediately = it == AppLifecycleProvider.State.Foreground, failure = false) }
            .catch { CoreLogger.e(LogTag.COLLECT_ERROR, it) }
            .collect()
    }

    private suspend fun internalStart() {
        CoreLogger.i(LogTag.DEFAULT, "EventManager internalStart $config")
        if (isStarted) return
        observeJob = scopeProvider.GlobalDefaultSupervisedScope.launch { collectStateChanges() }
        // Now isStarted === true === observeJob.isActive.
    }

    private suspend fun internalStop() {
        CoreLogger.i(LogTag.DEFAULT, "EventManager internalStop $config")
        try {
            eventWorkerManager.cancel(config)
        } finally {
            eventMetadataRepository.updateState(config, State.Cancelled)
            eventMetadataRepository.updateRetry(config, 0)
            observeJob?.cancel()
        }
        // Now isStarted === false === observeJob.isActive.
    }

    private suspend fun <R> internalSuspend(block: suspend () -> R): R {
        return if (!isStarted) {
            block.invoke()
        } else {
            internalStop()
            try {
                block.invoke()
            } finally {
                internalStart()
            }
        }
    }

    override val config: EventManagerConfig = deserializer.config
    override val isStarted: Boolean get() = observeJob?.isActive ?: false

    override suspend fun start() {
        lock.withLock { internalStart() }
    }

    override suspend fun stop() {
        lock.withLock { internalStop() }
    }

    override suspend fun resume() {
        lock.withLock { enqueueOrStop(immediately = true, failure = false) }
    }

    override suspend fun <R> suspend(block: suspend () -> R): R {
        lock.withLock { return internalSuspend(block) }
    }

    override fun subscribe(eventListener: EventListener<*, *>) {
        eventListenersByOrder.getOrPut(eventListener.order) { mutableSetOf() }.add(eventListener)
    }

    override suspend fun process() = processFirstUncompleted()

    override suspend fun getLatestEventId(): EventId =
        eventMetadataRepository.getLatestEventId(config.userId, deserializer.getLatestEventIdEndpoint)
            .let { deserializer.deserializeLatestEventId(it) }

    override suspend fun getEventResponse(eventId: EventId): EventsResponse =
        eventMetadataRepository.getEvents(config, eventId, deserializer.getEventsEndpoint)

    override suspend fun deserializeEventMetadata(eventId: EventId, response: EventsResponse): EventMetadata =
        deserializer.deserializeEventMetadata(eventId, response)

    companion object {
        private const val minRetries = 3
        const val retriesBeforeNotifyFailure = minRetries
        const val retriesBeforeNotifyResetAll = retriesBeforeNotifyFailure + minRetries
        const val retriesBeforeDeleteAllMetadata = retriesBeforeNotifyResetAll + minRetries
    }
}
