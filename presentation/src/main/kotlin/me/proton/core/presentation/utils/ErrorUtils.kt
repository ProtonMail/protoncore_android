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
import me.proton.core.network.domain.exception.ApiConnectionException
import me.proton.core.presentation.R
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

/**
 * Return localised and user readable error message.
 */
fun Throwable.getUserMessage(resources: Resources): String? =
    when (this) {
        // all connectivity errors are wrapped with ApiConnectionException under ApiException
        is ApiException ->
            if (cause is ApiConnectionException) {
                (cause as ApiConnectionException).getUserMessage(resources)
            } else message
        // currently all other errors return their original message
        else -> message
    }

internal fun ApiConnectionException.getUserMessage(resources: Resources): String? =
    when (this.cause) {
        is SSLHandshakeException,
        is SSLPeerUnverifiedException,
        is SocketTimeoutException,
        is UnknownHostException -> resources.getString(R.string.presentation_general_connection_error)
        else -> message
    }
