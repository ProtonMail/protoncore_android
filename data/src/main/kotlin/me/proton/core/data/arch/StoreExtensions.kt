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

package me.proton.core.data.arch

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import com.dropbox.android.external.store4.fresh
import com.dropbox.android.external.store4.get
import me.proton.core.util.kotlin.CoroutineScopeProvider
import kotlin.coroutines.cancellation.CancellationException

private suspend fun <T> catchWithStackTrace(block: suspend () -> T): T {
    // An exception from Store fetcher will result in a partial stacktrace, due to the asynchronicity of Store.
    // More info: https://github.com/Kotlin/kotlinx.coroutines/blob/master/docs/topics/debugging.md#stacktrace-recovery.
    // Remember current stacktrace to append it to potential inner throwable.
    val initialStackTrace = Throwable().stackTrace
    return runCatching {
        block()
    }.getOrElse {
        when (it) {
            is CancellationException -> throw it
            else -> throw it.apply { stackTrace += initialStackTrace }
        }
    }
}

/**
 * [Store] that can keep track of the stacktrace of fresh and get calls.
 */
class ProtonStore<Key : Any, Output : Any>(
    private val store: Store<Key, Output>,
) : Store<Key, Output> by store {
    suspend fun fresh(key: Key): Output = catchWithStackTrace { store.fresh(key) }
    suspend fun get(key: Key): Output = catchWithStackTrace { store.get(key) }
}

/**
 * Used to build a [ProtonStore] instead of the default [RealStore].
 */
fun <Key : Any, Output : Any> StoreBuilder<Key, Output>.buildProtonStore(scopeProvider: CoroutineScopeProvider) =
    ProtonStore(scope(scopeProvider.GlobalIOSupervisedScope).build())
