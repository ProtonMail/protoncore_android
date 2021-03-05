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

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.di.ApiFactory
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Provide [ApiManager] instance bound to a specific [SessionId].
 */
class ApiProvider(
    val apiFactory: ApiFactory,
    val sessionProvider: SessionProvider
) {
    val instances: ConcurrentHashMap<String, ConcurrentHashMap<String, Reference<ApiManager<*>>>> =
        ConcurrentHashMap()

    suspend inline fun <reified Api : BaseRetrofitApi> get(
        userId: UserId
    ): ApiManager<out Api> = get(sessionProvider.getSessionId(userId))

    inline fun <reified Api : BaseRetrofitApi> get(
        sessionId: SessionId? = null
    ): ApiManager<out Api> {
        // ConcurrentHashMap does not allow null to be used as a key or value.
        // If sessionId == null -> sessionName = "null".
        // We still want to store an instance if sessionId == null.
        val sessionName = sessionId?.id.toString()
        val className = Api::class.java.name
        return instances
            .getOrPut(sessionName) { ConcurrentHashMap() }
            .getOrPutWeakRef(className) {
                apiFactory.create(sessionId = sessionId, interfaceClass = Api::class)
            } as ApiManager<out Api>
    }

    fun <K, V> ConcurrentMap<K, Reference<V>>.getOrPutWeakRef(key: K, defaultValue: () -> V): V =
        this[key]?.get() ?: defaultValue().apply { put(key, WeakReference(this)) }
}
