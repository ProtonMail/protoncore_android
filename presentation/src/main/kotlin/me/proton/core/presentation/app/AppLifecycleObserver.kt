/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.presentation.app

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

open class AppLifecycleObserver : AppLifecycleProvider, DefaultLifecycleObserver {

    private val mutableSharedState = MutableSharedFlow<AppLifecycleProvider.State>(
        replay = 1,
        onBufferOverflow = BufferOverflow.SUSPEND
    )

    override val lifecycle: Lifecycle by lazy {
        ProcessLifecycleOwner.get().lifecycle
    }

    override val state: StateFlow<AppLifecycleProvider.State> by lazy {
        mutableSharedState
            .onSubscription { withContext(Dispatchers.Main) { lifecycle.addObserver(this@AppLifecycleObserver) } }
            .stateIn(lifecycle.coroutineScope, SharingStarted.Lazily, AppLifecycleProvider.State.Background)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        mutableSharedState.tryEmit(AppLifecycleProvider.State.Foreground)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        mutableSharedState.tryEmit(AppLifecycleProvider.State.Background)
    }
}
