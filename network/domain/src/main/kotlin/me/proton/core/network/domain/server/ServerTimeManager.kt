/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.network.domain.server

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Clock

class ServerTimeManager(private val callback: (Long) -> Unit) : ServerTimeListener {
    private val _offsetMilliseconds = MutableStateFlow<Long>(0)

    val offsetMilliseconds: StateFlow<Long> = _offsetMilliseconds

    override fun onServerTimeMillisUpdated(epochMillis: Long) {
        callback.invoke(epochMillis)
        val currentTime = Clock.systemUTC().millis()
        _offsetMilliseconds.value = epochMillis - currentTime
    }
}