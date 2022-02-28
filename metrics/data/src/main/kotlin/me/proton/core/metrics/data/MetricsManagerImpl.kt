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

package me.proton.core.metrics.data

import androidx.work.WorkManager
import me.proton.core.domain.entity.UserId
import me.proton.core.metrics.data.remote.worker.PostMetricsWorker
import me.proton.core.metrics.domain.MetricsManager
import me.proton.core.metrics.domain.entity.Metrics
import javax.inject.Inject

public class MetricsManagerImpl @Inject constructor(
    private val workManager: WorkManager
) : MetricsManager {

    /**
     * This will pass the metrics to a worker via
     * its input data, hence the metrics can't be
     * arbitrarily large.
     */
    override fun send(userId: UserId?, metrics: Metrics) {
        workManager.enqueue(
            PostMetricsWorker.getRequest(userId, metrics)
        )
    }
}
