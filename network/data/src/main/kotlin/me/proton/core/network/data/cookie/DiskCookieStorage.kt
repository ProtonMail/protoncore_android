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

package me.proton.core.network.data.cookie

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.serialize
import okhttp3.Cookie
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Singleton

@Singleton
class DiskCookieStorage(
    context: Context,
    storeName: String,
    scopeProvider: CoroutineScopeProvider
) : CookieStorage {

    companion object {
        // Handle Singleton here due to Parametrized/Tests.
        internal val instances = mutableMapOf<String, DataStore<SerializableCookies>>()
    }

    private val Context.dataStore by dataStore(
        storeName,
        scope = scopeProvider.GlobalIOSupervisedScope,
        serializer = SerializableCookiesSerializer(),
        corruptionHandler = ReplaceFileCorruptionHandler { SerializableCookies(emptyMap()) }
    )

    private val dataStore: DataStore<SerializableCookies> =
        instances.getOrPut(storeName) { context.dataStore }

    override fun all(): Flow<Cookie> = flow {
        dataStore.data.first().map.values.forEach {
            emit(it.toOkHttpCookie())
        }
    }

    override suspend fun set(cookie: Cookie) {
        dataStore.edit {
            it[cookie.key()] = cookie.toSerializableCookie()
        }
    }

    override suspend fun remove(cookie: Cookie) {
        dataStore.edit {
            it.remove(cookie.key())
        }
    }

    private suspend fun DataStore<SerializableCookies>.edit(
        updater: (MutableMap<CookieKey, SerializableCookie>) -> Unit
    ) {
        updateData {
            val updatedMap = it.map.toMutableMap()
            updater(updatedMap)
            it.copy(map = updatedMap)
        }
    }
}

private class SerializableCookiesSerializer : Serializer<SerializableCookies> {
    override val defaultValue: SerializableCookies
        get() = SerializableCookies(mapOf())

    override suspend fun readFrom(input: InputStream): SerializableCookies =
        try {
            input.bufferedReader().use {
                it.readText().deserialize()
            }
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }

    override suspend fun writeTo(t: SerializableCookies, output: OutputStream) {
        output.bufferedWriter().use {
            // This method is executed on IO dispatcher (DiskCookieStorage.scope)
            @Suppress("BlockingMethodInNonBlockingContext")
            it.write(t.serialize())
        }
    }
}
