/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.domain.usecase

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.timeout
import me.proton.core.devicemigration.domain.entity.EdmCodeResult
import me.proton.core.network.domain.session.SessionId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

public class ObserveEdmCode(
    private val generateEdmCode: GenerateEdmCode
) {
    @OptIn(FlowPreview::class)
    public operator fun invoke(
        sessionId: SessionId?,
        autoRefreshDuration: Duration = 10.minutes
    ): Flow<EdmCodeResult> = flow {
        emit(generateEdmCode(sessionId))
        awaitCancellation()
    }.timeout(autoRefreshDuration).retryWhen { cause, _ ->
        cause is TimeoutCancellationException
    }
}
