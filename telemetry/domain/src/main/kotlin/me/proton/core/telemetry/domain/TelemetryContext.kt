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

package me.proton.core.telemetry.domain

import me.proton.core.domain.entity.UserId
import me.proton.core.telemetry.domain.entity.TelemetryEvent
import me.proton.core.util.kotlin.coroutine.ResultCollector

public interface TelemetryContext {

    public val telemetryManager: TelemetryManager

    public fun enqueueTelemetry(
        userId: UserId? = null,
        data: TelemetryEvent
    ): Unit = telemetryManager.enqueue(userId, data)

    public fun <T> Result<T>.enqueueTelemetry(
        userId: UserId? = null,
        block: Result<T>.() -> TelemetryEvent
    ): Result<T> = also { enqueueTelemetry(userId, block(this)) }

    public suspend fun <T> ResultCollector<T>.onResultEnqueueTelemetry(
        key: String,
        userId: UserId? = null,
        block: Result<T>.() -> TelemetryEvent
    ): Unit = onResult(key) { enqueueTelemetry(userId, block) }

    public suspend fun <T> ResultCollector<T>.onCompleteEnqueueTelemetry(
        userId: UserId? = null,
        block: Result<T>.() -> TelemetryEvent
    ): Unit = onComplete { enqueueTelemetry(userId, block) }
}
