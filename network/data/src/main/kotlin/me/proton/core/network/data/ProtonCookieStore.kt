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

package me.proton.core.network.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.proton.core.network.data.cookie.CookieStorage
import me.proton.core.network.data.cookie.DiskCookieStorage
import me.proton.core.network.data.cookie.MemoryCookieStorage
import me.proton.core.network.data.cookie.hasExpired
import me.proton.core.util.kotlin.CoroutineScopeProvider
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * @param persistentStorage Storage for persistent cookies.
 * @param sessionStorage Storage for session (non-persistent) cookies.
 */
class ProtonCookieStore constructor(
    private val persistentStorage: CookieStorage,
    private val sessionStorage: CookieStorage,
) : CookieJar {
    constructor(context: Context, scopeProvider: CoroutineScopeProvider) : this(
        persistentStorage = DiskCookieStorage(context, DISK_COOKIE_STORAGE_NAME, scopeProvider),
        sessionStorage = MemoryCookieStorage()
    )

    override fun loadForRequest(url: HttpUrl): List<Cookie> = runBlocking {
        get(url).toList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) = runBlocking {
        cookies.forEach { storeCookie(it) }
    }

    internal fun all(): Flow<Cookie> = flow {
        emitAll(persistentStorage.all())
        emitAll(sessionStorage.all())
    }

    internal fun get(forUrl: HttpUrl): Flow<Cookie> =
        all().filter { !it.hasExpired() && it.matches(forUrl) }

    private suspend fun storeCookie(cookie: Cookie) {
        persistentStorage.remove(cookie)
        sessionStorage.remove(cookie)

        if (!cookie.hasExpired()) {
            if (cookie.persistent) {
                persistentStorage.set(cookie)
            } else {
                sessionStorage.set(cookie)
            }
        }
    }

    companion object {
        const val DISK_COOKIE_STORAGE_NAME: String = "protonCookieStore"
    }
}
