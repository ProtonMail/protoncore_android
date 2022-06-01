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

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.protonApi.BaseRetrofitApi
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.DispatcherProvider
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * Provide [ApiManager] instance bound to a specific [SessionId].
 */
@Singleton
class ApiProvider @Inject constructor(
    val apiManagerFactory: ApiManagerFactory,
    val sessionProvider: SessionProvider,
    private val dispatcherProvider: DispatcherProvider
) {
    private val instancesMutex = Mutex()
    private val instances: ConcurrentHashMap<String, ConcurrentHashMap<String, Reference<ApiManager<*>>>> =
        ConcurrentHashMap()

    suspend inline fun <reified Api : BaseRetrofitApi> get(
        userId: UserId?
    ): ApiManager<out Api> {
        val sessionId = userId?.let {
            sessionProvider.getSessionId(userId)
        }
        return get(sessionId = sessionId)
    }

    suspend inline fun <reified Api : BaseRetrofitApi> get(
        sessionId: SessionId? = null
    ): ApiManager<out Api> = get(Api::class, sessionId)

    suspend fun <Api : BaseRetrofitApi> get(
        apiClass: KClass<Api>,
        sessionId: SessionId? = null
    ): ApiManager<out Api> = withContext(dispatcherProvider.Io) {
        instancesMutex.withLock { blockingGet(apiClass, sessionId) }
    }

    private fun <Api : BaseRetrofitApi> blockingGet(
        apiClass: KClass<Api>,
        sessionId: SessionId? = null
    ): ApiManager<out Api> {
        // ConcurrentHashMap does not allow null to be used as a key or value.
        // If sessionId == null -> sessionName = "null".
        // We still want to store an instance if sessionId == null.
        val sessionName = sessionId?.id.toString()
        val className = apiClass.java.name
        return instances
            .getOrPut(sessionName) { ConcurrentHashMap() }
            .getOrPutWeakRef(className) {
                apiManagerFactory.create(sessionId = sessionId, interfaceClass = apiClass)
            } as ApiManager<out Api>
    }

    private fun <K, V> ConcurrentMap<K, Reference<V>>.getOrPutWeakRef(key: K, defaultValue: () -> V): V =
        this[key]?.get() ?: defaultValue().apply { put(key, WeakReference(this)) }
}
