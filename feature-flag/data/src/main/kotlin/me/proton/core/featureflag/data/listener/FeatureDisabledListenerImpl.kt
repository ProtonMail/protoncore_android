/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.featureflag.data.listener

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.network.domain.feature.FeatureDisabledListener
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import javax.inject.Inject
import javax.inject.Provider
import kotlin.time.Duration

public class FeatureDisabledListenerImpl @Inject constructor(
    private val featureFlagRepositoryProvider: Provider<FeatureFlagRepository>,
    private val sessionProvider: SessionProvider,
    private val minimumFetchInterval: Duration,
    private val monoClock: () -> Long,
) : FeatureDisabledListener {
    private var lastFetchTimestampMs: Long? = null
    private val mutex = Mutex()

    override suspend fun onFeatureDisabled(sessionId: SessionId?) {
        if (shouldFetch()) {
            featureFlagRepositoryProvider.get()?.getAll(sessionId?.let { sessionProvider.getUserId(sessionId) })
            updateLastFetchTimestamp()
        }
    }

    private suspend fun shouldFetch(): Boolean = getLastFetchTimestampMs()?.let { lastFetch ->
        monoClock() - lastFetch > minimumFetchInterval.inWholeMilliseconds
    } ?: true

    private suspend fun getLastFetchTimestampMs(): Long? = mutex.withLock {
        lastFetchTimestampMs
    }

    private suspend fun updateLastFetchTimestamp() {
        mutex.withLock {
            lastFetchTimestampMs = monoClock()
        }
    }
}
