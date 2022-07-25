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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppLifecycleObserver : AppLifecycleProvider, DefaultLifecycleObserver {

    private val mutableState = MutableStateFlow(AppLifecycleProvider.State.Background)

    override val lifecycle: Lifecycle by lazy {
        ProcessLifecycleOwner.get().lifecycle
    }

    override val state: StateFlow<AppLifecycleProvider.State> = mutableState.asStateFlow()

    init {
        lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        mutableState.tryEmit(AppLifecycleProvider.State.Foreground)
    }

    override fun onStop(owner: LifecycleOwner) {
        mutableState.tryEmit(AppLifecycleProvider.State.Background)
    }
}
