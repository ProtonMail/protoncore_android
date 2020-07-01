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
package me.proton.core.network.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus

/**
 * Implementation of [NetworkManager] based on Android's [ConnectivityManager]
 */
internal class NetworkManagerImpl(
    private val context: Context
) : NetworkManager() {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var registered = false

    private val connectivityBroadcastReceiver by lazy {
        object : BroadcastReceiver() {

            /** Called when connectivity has changed */
            override fun onReceive(context: Context, intent: Intent) {
                notifyObservers(networkStatus)
            }
        }
    }

    override val networkStatus: NetworkStatus get() = with(connectivityManager) {
        when {
            activeNetworkInfo?.isConnected != true -> NetworkStatus.Disconnected
            isActiveNetworkMetered -> NetworkStatus.Metered
            else -> NetworkStatus.Unmetered
        }
    }

    override fun register() {
        if (!registered) {
            registered = true
            context.registerReceiver(
                connectivityBroadcastReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    override fun unregister() {
        if (registered) {
            registered = false
            context.unregisterReceiver(connectivityBroadcastReceiver)
        }
    }
}
