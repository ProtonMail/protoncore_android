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

package me.proton.android.core.data.api.entity

/**
 * Created by dinokadrikj on 4/6/20.
 *
 * Singleton object for holding the server time, which is crucial for some business logic in some
 * of the products.
 */
// TODO: this class might need to be moved to another module. Initially it should stay here.
object ServerTime {
    private var serverTime: Long = 0
    private var lastClientTime: Long = 0

    fun updateServerTime(serverTime: Long) {
        ServerTime.serverTime = serverTime
        lastClientTime = System.nanoTime()
    }

    fun currentTimeMillis(): Long {
        if (serverTime == 0L) {
            return System.currentTimeMillis()
        }
        val timeDiff = (System.nanoTime() - lastClientTime) / 1000000
        return serverTime + timeDiff
    }
}