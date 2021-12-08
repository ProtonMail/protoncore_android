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

package me.proton.core.report.presentation.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * @param email Email of the user that is sending the report (usually from the primary account).
 * @param username Username of the user that is sending the report (usually from the primary account).
 * @param country Optional country of the user (VPN).
 * @param isp Optional Internet Service Provider (VPN).
 * @param finishAfterReportIsEnqueued If `true`, the report will be enqueued,
 *  and sent in the background via work manager;
 *  otherwise, the UI will wait until the report is received by the server.
 */
@Parcelize
public data class BugReportInput(
    val email: String,
    val username: String,
    val country: String? = null,
    val isp: String? = null,
    val finishAfterReportIsEnqueued: Boolean = true
) : Parcelable
