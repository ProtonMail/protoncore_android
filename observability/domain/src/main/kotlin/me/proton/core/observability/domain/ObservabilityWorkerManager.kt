/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.observability.domain

import kotlin.time.Duration

public interface ObservabilityWorkerManager {
    /** Cancels any scheduled workers.
     * Will not cancel a worker if it's already running.
     */
    public fun cancel()

    /** Returns the duration since the last successful shipment of observability events.
     * Returns `null` if the shipment wasn't recorder yet.
     */
    public suspend fun getDurationSinceLastShipment(): Duration?

    /** Marks that observability events have been successfully shipped (sent). */
    public suspend fun setLastSentNow()

    /** Schedules a worker to send the observability events.
     * If a worker has been previously scheduled but hasn't yet executed,
     * the existing scheduled worker will be kept.
     */
    public fun schedule(delay: Duration)
}
