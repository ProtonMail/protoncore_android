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

package me.proton.core.reports.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.reports.domain.entity.BugReport
import me.proton.core.reports.domain.entity.BugReportExtra

public interface SendBugReport {
    public suspend fun cancel(requestId: String)

    public operator fun invoke(
        userId: UserId,
        bugReport: BugReport,
        extra: BugReportExtra? = null
    ): Flow<Result>

    public sealed class Result {
        public abstract val requestId: String

        /** Request for sending bug report has been created but not yet enqueued. */
        public data class Initialized(override val requestId: String) : Result()

        /** Request has been enqueued and will be sent automatically once network connection is available. */
        public data class Enqueued(override val requestId: String) : Result()

        /** Request is temporarily blocked, as network connection is not available. */
        public data class Blocked(override val requestId: String) : Result()

        /** Bug report has been successfully sent. */
        public data class Sent(override val requestId: String) : Result()

        /** Request for sending bug report was [cancelled][cancel] and will not be sent. */
        public data class Cancelled(override val requestId: String) : Result()

        /** Request has failed and will not be retried. */
        public data class Failed(override val requestId: String, public val message: String?) : Result()
    }
}
