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

package me.proton.core.eventmanager.domain

object LogTag {
    /** Default tag for this module. */
    const val DEFAULT = "core.eventmanager"

    /** Tag for Worker Errors. */
    const val WORKER_ERROR = "core.eventmanager.worker"

    /** Tag for Notify Errors. */
    const val NOTIFY_ERROR = "core.eventmanager.notify"

    /** Tag for Fetch Errors. */
    const val FETCH_ERROR = "core.eventmanager.fetch"

    /** Tag for Collect Errors. */
    const val COLLECT_ERROR = "core.eventmanager.collect"

    /** Tag for Max Retry Reports. */
    const val REPORT_MAX_RETRY = "core.eventmanager.report.maxretry"
}
