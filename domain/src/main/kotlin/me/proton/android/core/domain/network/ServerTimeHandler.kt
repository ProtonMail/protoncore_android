/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.android.core.domain.network

import me.proton.android.core.domain.crypto.IOpenPGP
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by dinokadrikj on 4/14/20.
 *
 * Server time is usually needed for various operations and mostly for crypto operations. OpenPGP
 * crypto library depends on the server time, so it should be updated accordingly and on a regular
 * basis.
 * We can not rely on the device's time, for obvious reasons (timezone, users changing time etc).
 *
 * This class is an abstract class and the concrete implementation should be provided.
 */
abstract class ServerTimeHandler(protected val openPgp: IOpenPGP) {

    companion object {
        val RFC_1123_FORMAT: SimpleDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)

        init {
            RFC_1123_FORMAT.timeZone = TimeZone.getTimeZone("GMT")
        }
    }
}