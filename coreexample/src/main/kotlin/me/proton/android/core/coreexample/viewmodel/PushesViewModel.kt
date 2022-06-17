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

package me.proton.android.core.coreexample.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
class PushesViewModel @Inject constructor(
    private val pushRepository: PushRepository,
    private val accountManager: AccountManager,
) : ViewModel() {

    private val mutableState = MutableStateFlow<State>(State.Loading)
    val state = mutableState.asStateFlow().filterNotNull()

    private var observePushesJob: Job? = null

    init {
        observePrimaryAccountPushes()
    }

    private fun observePrimaryAccountPushes(refresh: Boolean = false) {
        mutableState.tryEmit(State.Loading)
        observePushesJob?.cancel()
        observePushesJob = viewModelScope.launch {
            accountManager.getPrimaryUserId().filterNotNull().collect { userId ->
                pushRepository.observeAllPushes(userId, PushObjectType.Messages, refresh = refresh).collect { result ->
                    CoreLogger.v("push", result.toString())
                    mutableState.value = State.Pushes(result)
                }
            }
        }
    }

    sealed class State {
        object Loading : State()
        data class Pushes(val pushes: List<Push>) : State()
    }
}
