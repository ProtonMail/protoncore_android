/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.network.domain.server

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * The AppClock class provides a corrected current time based on an offset from the ServerTimeListener.
 * ServerTimeListener receives updates from API.
 * This corrected time compensates for any discrepancies between the device time and the server time.
 *
 * @property offsetManager The manager that provides the time offset between server time and device time.
 */
class ServerClock(private val offsetManager: ServerTimeManager) {

    private val utcClock: Clock = Clock.systemUTC()
    private val localClock: Clock = Clock.systemDefaultZone()

    val utcTimeFlow: Flow<Instant> = generateFlow(utcClock)
    val timeFlow: Flow<Instant> = generateFlow(localClock)

    fun getCurrentTimeUTC(): Instant {
        val now = utcClock.instant()
        return getCorrectedTime(now)
    }

    fun getCurrentTime(): Instant {
        val now = localClock.instant()
        return getCorrectedTime(now)
    }

    private fun generateFlow(clock: Clock): Flow<Instant> = flow {
        val referenceTime = clock.instant()
        while (currentCoroutineContext().isActive) {
            val now = clock.instant()
            val correctedTime = now.plusMillis(offsetManager.offsetMilliseconds.value)
            val elapsedS = Duration.between(referenceTime, correctedTime).seconds
            emit(correctedTime)
            // Wait until the start of the next second before emitting the next value
            delay(Duration.between(now, referenceTime.plusSeconds(elapsedS + 1)).toMillis())
        }
    }

    private fun getCorrectedTime(now: Instant): Instant = now.plusMillis(offsetManager.offsetMilliseconds.value)
}