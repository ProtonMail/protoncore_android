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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.api.CoreExampleRepository
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.protonApi.isSuccess
import me.proton.core.push.domain.entity.Push
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.entity.PushObjectType
import me.proton.core.push.domain.repository.PushRepository
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
class PushDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pushRepository: PushRepository,
    private val coreExampleRepository: CoreExampleRepository,
) : ViewModel() {

    private val mutableState = MutableStateFlow<State?>(null)
    val state = mutableState.asStateFlow().filterNotNull()

    private val userId = UserId(requireNotNull(savedStateHandle.get(ARG_USER_ID)))
    private val pushId = PushId(requireNotNull(savedStateHandle.get(ARG_PUSH_ID)))

    private var observePushJob: Job? = null

    init {
        observePushJob = viewModelScope.launch { observePush() }
    }

    private suspend fun observePush() {
        pushRepository.observeAllPushes(userId, PushObjectType.Messages).collect { pushes ->
            val result = pushes.find { it.pushId == pushId }
            CoreLogger.v("push", result.toString())
            if (result != null) {
                mutableState.value = State.PushContent(result)
            } else {
                mutableState.value = State.Finish("not found")
            }
        }
    }

    fun onClickDeletePush() {
        viewModelScope.launch {
            observePushJob?.cancel()
            pushRepository.deletePush(userId, pushId)
            mutableState.value = State.Finish("push deleted")
        }
    }

    fun onClickMarkAsRead() {
        viewModelScope.launch {
            val pushes = pushRepository.getAllPushes(userId, PushObjectType.Messages)
            pushes.find { it.pushId == pushId }?.let {
                val success = coreExampleRepository.markAsRead(userId, it.objectId).isSuccess()
                if (success) {
                    mutableState.value = State.Finish("message marked as read succeed")
                } else {
                    mutableState.value = State.Finish("message marked as read failed.")
                }
            }
        }
    }

    sealed class State {
        data class PushContent(val push: Push) : State()
        data class Finish(val reason: String) : State()
    }

    companion object {
        const val ARG_USER_ID = "arg.userId"
        const val ARG_PUSH_ID = "arg.pushId"
    }
}
