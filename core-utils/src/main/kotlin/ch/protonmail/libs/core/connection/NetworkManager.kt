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

@file:Suppress(
    "FunctionName", // Constructor functions
    "EXPERIMENTAL_API_USAGE" // Coroutines Flow
)
package ch.protonmail.libs.core.connection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.LifecycleOwner
import ch.protonmail.libs.core.connection.NetworkStatus.*
import ch.protonmail.libs.core.utils.Android
import ch.protonmail.libs.core.utils.doOnDestroy
import ch.protonmail.libs.core.utils.internal.connectivityManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * This allows us to know whether network is available and also allows us to disable for our app.
 * @see networkEnabled
 * @see canUseNetwork
 *
 * @author Davide Farella
 */
interface NetworkManager {

    /** Whether network is enabled by the user */
    var networkEnabled : Boolean

    /** @return `true` if [isConnectedToNetwork] and [networkEnabled] are both `true` */
    fun canUseNetwork() = networkEnabled && isConnectedToNetwork()

    /** @return `true`is network connectivity is available */
    fun isConnectedToNetwork() : Boolean

    /** @return `true` if we are registered to network changes */
    fun isRegistered() : Boolean

    /**
     * Observe the [NetworkStatus] changes
     * [register] will be called if [isRegistered] if `false`
     */
    fun observe(callback: NetworkCallback) {
        if (!isRegistered()) register()
    }

    /**
     * @return [Flow] of [NetworkStatus] changes
     * No need to call [register] and [unregister] since they will be called automatically by the
     * Coroutine
     */
    fun observe() = callbackFlow {
        observe { offer(it) }
        awaitClose { unregister(clearCallback = true) }
    }

    /**
     * Observe the [NetworkStatus] changes accordingly to the Lifecycle of the given [lifecycleOwner]
     * @see registerWith
     * @see observe
     */
    @Deprecated("This class is not intended to be used inside the UI. Will be removed in 0.3")
    // TODO: remove in 0.3
    fun observeWith(lifecycleOwner: LifecycleOwner, clearCallback: Boolean = false, callback: NetworkCallback) {
        registerWith(lifecycleOwner)
        observe(callback)
    }

    /**
     * Register undefinitely an observer for the network state.
     * Use [unregister] for stop listening
     *
     * @throws IllegalStateException if already registered
     */
    fun register() {
        if (isRegistered()) throw IllegalStateException(
            "NetworkManager is already registered, ensure tu call 'register' from a single instance"
        )
    }

    /**
     * Register an observer for the network state, for the lifespan of [LifecycleOwner].
     * In this case calling [unregister] is not needed.
     *
     * Note that once the [LifecycleOwner] pass the `ON_DESTROY` event, the observer will be removed, so
     * [register] will NOT be called automatically after its eventual recreation.
     *
     * Note also that this won't create a new observer, but will just call [register] and [unregister] regarding the
     * Lifecycle. Make sure this won't clash with other [register] / [unregister] invocations; in that case
     * a new instance should be created for the single component
     *
     * @param clearCallback if `true` will remove the callback set via [observe] or [observeWith], when
     * [unregister] is called
     * Default is `false`
     */
    @Deprecated("This class is not intended to be used inside the UI. Will be removed in 0.3")
    // TODO: remove in 0.3
    fun registerWith(lifecycleOwner: LifecycleOwner, clearCallback: Boolean = false)

    /**
     * Remove the observer for the network state
     * @param clearCallback if `true` will remove the callback set via [observe] or [observeWith]
     * Default is `false`
     */
    fun unregister(clearCallback: Boolean = false)
}

/** @return [NetworkManager] */
fun NetworkManager(
    context: Context,
    connectivityManager: ConnectivityManager? = context.connectivityManager,
    networkEnabled: Boolean = true
) : NetworkManager =
    NetworkManagerImpl(
        context,
        connectivityManager,
        networkEnabled
    )

/** Implementation on [NetworkManager] by using [ConnectivityManager] */
internal class NetworkManagerImpl internal constructor(
    private val context: Context,
    private val connectivityManager: ConnectivityManager?,
    override var networkEnabled: Boolean
) : NetworkManager {

    /** Listener to [NetworkStatus] */
    private var listener: NetworkCallback = {}

    /**
     * [BroadcastReceiver] which will receive connectivity changes
     * It's lazy and it will be initialized only below API 23
     */
    private val connectivityBroadCastReceiver by lazy {
        object : BroadcastReceiver() {

            /** Called when connectivity has changed */
            override fun onReceive(context: Context, intent: Intent) = withConnectivityManager {
                val status = when {
                    !isConnectedToNetwork() -> Disconnected
                    isActiveNetworkMetered -> Metered
                    else -> Unmetered
                }
                listener(status)
            } ?: Unit
        }
    }

    private var registered = false

    /** @return `false` if [connectivityManager] is not available */
    override fun isConnectedToNetwork(): Boolean =
        withConnectivityManager {
            activeNetworkInfo.isConnected
        } ?: false

    override fun isRegistered() = registered

    override fun observe(callback: NetworkCallback) {
        super.observe(callback)
        listener = callback
    }

    override fun register() {
        super.register()
        registered = true
        registerCompat()
    }

    override fun registerWith(lifecycleOwner: LifecycleOwner, clearCallback: Boolean /* default is `false` */) {
        register()
        lifecycleOwner.doOnDestroy(removeObserver = true) { unregister(clearCallback) }
    }

    /** Register [connectivityBroadCastReceiver] on pre [Android.LOLLIPOP] devices */
    @Suppress("DEPRECATION") // Low level API
    private fun registerCompat() {
        context.registerReceiver(
            connectivityBroadCastReceiver,
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    override fun unregister(clearCallback: Boolean /* default is `false` */) {
        registered = false
        if (clearCallback) listener = {}
        /*if (Android.LOLLIPOP) unregisterLollipop() else */unregisterCompat()
    }

    /** Register [connectivityBroadCastReceiver] on pre [Android.LOLLIPOP] devices */
    private fun unregisterCompat() {
        context.unregisterReceiver(connectivityBroadCastReceiver)
    }

    /** Execute the lambda [block] if [connectivityManager] is not `null`, else handle it */
    private inline fun <T> withConnectivityManager(block: ConnectivityManager.() -> T) : T? {
        return if (connectivityManager != null) block(connectivityManager)
        else TODO("Handle null connectivityManager")
    }
}


/** Different status of Network */
enum class NetworkStatus { Unmetered, Metered, Disconnected }

/** Typealias for a lambda that takes [NetworkStatus] as argument */
typealias NetworkCallback = (NetworkStatus) -> Unit
