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

package me.proton.core.presentation.utils

import android.content.res.Resources
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.presentation.R

/**
 * Return localised and user readable error message.
 */
@Suppress("UseIfInsteadOfWhen")
fun Throwable.getUserMessage(resources: Resources): String? = when (this) {
    // All api errors are wrapped with ApiException.
    is ApiException -> getUserMessage(resources)
    // Currently all other errors return their original message.
    else -> message
}

internal fun ApiException.getUserMessage(resources: Resources): String? = when (error) {
    is ApiResult.Error.Certificate,
    is ApiResult.Error.Connection,
    is ApiResult.Error.Timeout -> resources.getString(R.string.presentation_general_connection_error)
    is ApiResult.Error.Http,
    is ApiResult.Error.Parse -> message
}
