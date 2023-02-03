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

package me.proton.core.observability.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import me.proton.core.observability.domain.usecase.ProcessObservabilityEvents
import me.proton.core.util.kotlin.CoreLogger

@HiltWorker
internal class ObservabilityWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val processObservabilityEvents: ProcessObservabilityEvents
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return processObservabilityEvents.runCatching {
            invoke()
            Result.success()
        }.recover {
            if (it is ApiException && it.isRetryable()) {
                Result.retry()
            } else {
                if (it !is ApiException) { // ApiExceptions are logged upstream already.
                    CoreLogger.e("ObservabilityWorker", it, "Could not send observability events.")
                }
                Result.failure(workDataOf("errorMessage" to it.message))
            }
        }.getOrThrow()
    }
}
