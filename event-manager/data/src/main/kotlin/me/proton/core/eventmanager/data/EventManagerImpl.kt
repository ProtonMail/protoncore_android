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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerializationException
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.eventmanager.data.extension.runCatching
import me.proton.core.eventmanager.data.extension.runInTransaction
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManager
import me.proton.core.eventmanager.domain.EventManagerConfig
import me.proton.core.eventmanager.domain.entity.Event
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
import me.proton.core.presentation.app.AppLifecycleProvider
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.exhaustive

@AssistedFactory
interface EventManagerFactory {
    fun create(deserializer: EventDeserializer): EventManagerImpl
}

class EventManagerImpl @AssistedInject constructor(
    @EventManagerCoroutineScope private val coroutineScope: CoroutineScope,
    private val appLifecycleProvider: AppLifecycleProvider,
    private val accountManager: AccountManager,
    private val eventWorkerManager: EventWorkerManager,
    internal val eventMetadataRepository: EventMetadataRepository,
    @Assisted val deserializer: EventDeserializer
) : EventManager {

    private val lock = Mutex()

    private var observeAccountJob: Job? = null
    private var observeAppStateJob: Job? = null

    internal val eventListenersByOrder = sortedMapOf<Int, MutableSet<EventListener<*, *>>>()

    private suspend fun deserializeEventsByListener(
        response: EventsResponse
    ): Map<EventListener<*, *>, List<Event<*, *>>> {
        return eventListenersByOrder.values.flatten().associateWith { eventListener ->
            eventListener.deserializeEvents(config, response).orEmpty()
        }
    }

    private suspend fun processFirstFromConfig() {
        val metadata = eventMetadataRepository.get(config).firstOrNull() ?: return
        when {
            metadata.retry > retriesBeforeReset -> {
                reportFailure(metadata)
                reset()
            }
            metadata.retry > retriesBeforeNotifyResetAll -> {
                reportFailure(metadata)
                notifyResetAll(metadata)
            }
            else -> when (metadata.state) {
                State.Cancelled -> fetch(metadata)
                State.Enqueued -> fetch(metadata)
                State.Fetching -> fetch(metadata)
                State.Persisted -> notify(metadata)
                State.NotifyPrepare -> notifyPrepare(metadata)
                State.NotifyEvents -> notifyPrepare(metadata)
                State.NotifyResetAll -> notifyResetAll(metadata)
                State.NotifyComplete -> notifyComplete(metadata, true)
                State.Completed -> Unit
            }
        }
    }

    private suspend fun reportFailure(metadata: EventMetadata) {
        val list = eventMetadataRepository.get(config).map { it.copy(response = null) }
        CoreLogger.log(LogTag.REPORT_MAX_RETRY, "Max Failure reached (current: ${metadata.eventId}): $list")
    }

    private suspend fun reset() {
        eventMetadataRepository.deleteAll(config)
        enqueue(eventId = null, immediately = true)
    }

    private suspend fun fetch(metadata: EventMetadata) {
        val eventId = metadata.eventId ?: getLatestEventId()
        runCatching(
            config = config,
            eventId = eventId,
            processingState = State.Fetching,
            successState = State.Persisted,
            failureState = State.Enqueued
        ) {
            val response = getEventResponse(eventId)
            val deserializedMetadata = deserializeEventMetadata(eventId, response)
            eventMetadataRepository.update(deserializedMetadata)
            deserializedMetadata
        }.onFailure {
            when {
                // throw it -> Use the WorkManager RETRY mechanism (backoff + network constraint).
                it is ApiException && it.isForceUpdate() -> throw it
                it is ApiException && it.isRetryable().not() -> notifyResetAll(metadata)
                it is SerializationException -> notifyResetAll(metadata)
                else -> throw it
            }
        }.onSuccess {
            notify(it)
        }
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
        runCatching(
            config = config,
            eventId = requireNotNull(metadata.eventId),
            processingState = State.NotifyResetAll,
            successState = State.NotifyComplete,
            failureState = State.NotifyResetAll
        ) {
            // Fully sequential and ordered.
            eventListenersByOrder.values.flatten().forEach {
                it.notifyResetAll(config)
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueue(requireNotNull(metadata.eventId), immediately = true)
        }.onSuccess {
            notifyComplete(metadata, success = false)
        }
    }

    private suspend fun notifyPrepare(metadata: EventMetadata) {
        runCatching(
            config = config,
            eventId = requireNotNull(metadata.eventId),
            processingState = State.NotifyPrepare,
            successState = State.NotifyEvents,
            failureState = State.NotifyPrepare
        ) {
            // Set actions for all listeners.
            val eventsByListener = deserializeEventsByListener(requireNotNull(metadata.response))
            eventsByListener.forEach { (eventListener, list) ->
                eventListener.setActionMap(config, list as List<Nothing>)
            }
            // Notify prepare for all listeners.
            eventListenersByOrder.values.flatten().forEach { eventListener ->
                eventListener.notifyPrepare(config)
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueue(metadata.eventId, immediately = true)
        }.onSuccess {
            notifyEvents(metadata)
        }
    }

    private suspend fun notifyEvents(metadata: EventMetadata) {
        runInTransaction(
            config = config,
            eventId = requireNotNull(metadata.eventId),
            processingState = State.NotifyEvents,
            successState = State.NotifyComplete,
            failureState = State.NotifyPrepare
        ) {
            // Fully sequential and ordered.
            eventListenersByOrder.values.flatten().forEach { eventListener ->
                eventListener.notifyEvents(config)
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueue(metadata.eventId, immediately = true)
        }.onSuccess {
            notifyComplete(metadata, success = true)
        }
    }

    private suspend fun notifyComplete(metadata: EventMetadata, success: Boolean) {
        runCatching(
            config = config,
            eventId = requireNotNull(metadata.eventId),
            processingState = State.NotifyComplete,
            successState = State.Completed,
            failureState = State.Completed
        ) {
            // Fully sequential and ordered.
            eventListenersByOrder.values.flatten().forEach { eventListener ->
                if (success) {
                    eventListener.notifySuccess(config)
                } else {
                    eventListener.notifyFailure(config)
                }
                eventListener.notifyComplete(config)
            }
        }.onFailure {
            CoreLogger.e(LogTag.NOTIFY_ERROR, it)
            enqueue(metadata.nextEventId, immediately = metadata.more ?: false)
        }.onSuccess {
            enqueue(metadata.nextEventId, immediately = metadata.more ?: false)
        }
    }

    private suspend fun enqueue(eventId: EventId?, immediately: Boolean) {
        val metadata = eventId?.let { eventMetadataRepository.get(config, it) }
        eventMetadataRepository.update(
            metadata?.takeUnless { metadata.eventId == metadata.nextEventId }?.copy(
                retry = metadata.retry.plus(1)
            ) ?: EventMetadata(
                userId = config.userId,
                eventId = eventId,
                config = config,
                retry = 0,
                state = State.Enqueued,
                createdAt = System.currentTimeMillis()
            )
        )
        eventWorkerManager.enqueue(config, immediately)
    }

    private suspend fun enqueueOrCancel(account: Account?) {
        when {
            account == null || account.userId != config.userId -> cancel()
            account.state != AccountState.Ready -> cancel()
            eventMetadataRepository.get(config).isEmpty() -> enqueue(eventId = null, immediately = true)
            else -> eventWorkerManager.enqueue(config, immediately = true)
        }
    }

    private suspend fun cancel() {
        eventWorkerManager.cancel(config)
        eventMetadataRepository.updateState(config, State.Cancelled)
    }

    private suspend fun internalStart() {
        if (isStarted) return

        // Observe any Account changes.
        observeAccountJob = accountManager.getAccount(config.userId)
            .distinctUntilChangedBy { it?.state }
            .onEach { account -> enqueueOrCancel(account) }
            .launchIn(coroutineScope)

        // Observe any Foreground App State changes.
        observeAppStateJob = appLifecycleProvider.state
            .filter { it == AppLifecycleProvider.State.Foreground }
            .onEach { enqueueOrCancel(accountManager.getAccount(config.userId).firstOrNull()) }
            .launchIn(coroutineScope)

        isStarted = true
    }

    private fun internalStop() {
        if (!isStarted) return

        observeAccountJob?.cancel()
        observeAppStateJob?.cancel()
        eventWorkerManager.cancel(config)

        isStarted = false
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
    override var isStarted: Boolean = false

    override suspend fun start() {
        lock.withLock { internalStart() }
    }

    override suspend fun stop() {
        lock.withLock { internalStop() }
    }

    override suspend fun <R> suspend(block: suspend () -> R): R {
        lock.withLock { return internalSuspend(block) }
    }

    override fun subscribe(eventListener: EventListener<*, *>) {
        eventListenersByOrder.getOrPut(eventListener.order) { mutableSetOf() }.add(eventListener)
    }

    override suspend fun process() = processFirstFromConfig()

    override suspend fun getLatestEventId(): EventId =
        eventMetadataRepository.getLatestEventId(config.userId, deserializer.endpoint)
            .let { deserializer.deserializeLatestEventId(it) }

    override suspend fun getEventResponse(eventId: EventId): EventsResponse =
        eventMetadataRepository.getEvents(config.userId, eventId, deserializer.endpoint)

    override suspend fun deserializeEventMetadata(eventId: EventId, response: EventsResponse): EventMetadata =
        deserializer.deserializeEventMetadata(eventId, response)

    companion object {
        // Constraint: retriesBeforeNotifyResetAll < retriesBeforeReset.
        const val retriesBeforeNotifyResetAll = 3
        const val retriesBeforeReset = 6
    }
}
