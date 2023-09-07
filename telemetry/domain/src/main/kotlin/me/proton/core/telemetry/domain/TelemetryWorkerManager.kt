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
import kotlin.time.Duration

public interface TelemetryWorkerManager {
    /** Cancels any scheduled workers for a user.
     * @param userId the user id
     */
    public fun cancel(userId: UserId?)

    /** Schedules a worker to send the telemetry events for a user.
     * If a worker has been previously scheduled but hasn't yet executed,
     * the existing scheduled worker will be kept.
     * @param userId the user id
     */
    public fun enqueueOrKeep(userId: UserId?, delay: Duration)
}
