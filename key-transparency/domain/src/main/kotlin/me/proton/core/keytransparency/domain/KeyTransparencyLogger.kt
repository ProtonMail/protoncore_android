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

package me.proton.core.keytransparency.domain

import me.proton.core.util.kotlin.CoreLogger

private const val LOG_TAG = "me.proton.core.keytransparency"

public object KeyTransparencyLogger {

    public fun d(message: String) {
        CoreLogger.d(LOG_TAG, message)
    }

    public fun d(throwable: Throwable, message: String) {
        CoreLogger.d(LOG_TAG, throwable, message)
    }

    public fun e(throwable: Throwable) {
        CoreLogger.e(LOG_TAG, throwable)
    }

    public fun e(throwable: Throwable, message: String) {
        CoreLogger.e(LOG_TAG, throwable, message)
    }
}
