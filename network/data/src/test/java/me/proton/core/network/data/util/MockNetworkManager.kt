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
package me.proton.core.network.data.util

import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus

class MockNetworkManager : NetworkManager() {
    var registered = false

    override var networkStatus: NetworkStatus = NetworkStatus.Disconnected
        set(value) {
            field = value
            notifyObservers(value)
        }

    override fun register() {
        require(!registered)
        registered = true
    }

    override fun unregister() {
        require(registered)
        registered = false
    }
}
