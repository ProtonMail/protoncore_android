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

package me.proton.core.eventmanager.domain.work

import me.proton.core.eventmanager.domain.EventManagerConfig
import kotlin.time.Duration

interface EventWorkerManager {

    /**
     * Enqueue a Worker for this [config].
     *
     * @param immediately if true, start/process the task immediately, if possible.
     */
    suspend fun enqueue(config: EventManagerConfig, immediately: Boolean)

    /**
     * Cancel Worker for this [config].
     */
    suspend fun cancel(config: EventManagerConfig)

    /**
     * Returns true if Worker is running.
     */
    suspend fun isRunning(config: EventManagerConfig): Boolean

    /**
     * Returns true if Worker is enqueued.
     */
    suspend fun isEnqueued(config: EventManagerConfig): Boolean

    /**
     * Get immediate minimal initial delay for any Worker to start.
     */
    fun getImmediateMinimumInitialDelay(): Duration

    /**
     * Get repeat interval while app is in foreground.
     */
    fun getRepeatIntervalForeground(): Duration

    /**
     * Get repeat interval while app is in background.
     */
    fun getRepeatIntervalBackground(): Duration

    /**
     * Returns true is background interval should be considering the app standby bucket
     */
    fun repeatIntervalBackgroundByAppStandbyBucket(): Boolean

    /**
     * Get backoff delay.
     */
    fun getBackoffDelay(): Duration

    /**
     * Sets whether device battery should be at an acceptable level for the Worker to run.
     *
     * Note: This constraint is only applied when app is in background.
     */
    fun requiresBatteryNotLow(): Boolean

    /**
     * Sets whether the device's available storage should be at an acceptable level for the Worker to run.
     *
     * Note: This constraint is only applied when app is in background.
     */
    fun requiresStorageNotLow(): Boolean
}
