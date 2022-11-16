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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.proton.android.core.coreexample.utils.prettyPrint
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.entity.UserId
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelId
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.repository.LabelRepository
import me.proton.core.util.kotlin.random
import me.proton.core.util.kotlin.truncateToLength
import javax.inject.Inject

@HiltViewModel
class LabelDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val labelRepository: LabelRepository,
) : ActionViewModel<LabelDetailViewModel.Action, LabelDetailViewModel.State>() {

    private val userId = UserId(requireNotNull(savedStateHandle.get(ARG_USER_ID)))
    private val labelId = LabelId(requireNotNull(savedStateHandle.get(ARG_LABEL_ID)))
    private val labelType = requireNotNull(LabelType.Companion.map[savedStateHandle.get(ARG_LABEL_TYPE)])

    sealed class Action {
        object Observe : Action()
        object Update : Action()
        object Delete : Action()
    }

    sealed class State {
        object Loading : State()
        data class Success(val rawLabel: String) : State()
        data class Error(val error: String?) : State()
        object Updated : State()
        object Deleted : State()
    }

    init {
        dispatch(Action.Observe)
    }

    override fun process(action: Action): Flow<State> = when (action) {
        Action.Observe -> observeLabel()
        Action.Delete -> deleteLabel()
        Action.Update -> updateLabel()
    }

    private fun observeLabel(): Flow<State> = flow {
        emit(State.Loading)
        emitAll(labelRepository.observeLabels(userId, labelType).mapState())
    }

    private fun deleteLabel(): Flow<State> = flow {
        emit(State.Loading)
        labelRepository.deleteLabel(userId, labelType, labelId)
        emit(State.Deleted)
    }.catch {
        emit(State.Error(it.message))
    }

    private fun updateLabel(): Flow<State> = flow {
        emit(State.Loading)
        val updatedLabel = requireNotNull(labelRepository.getLabel(userId, labelType, labelId))
        labelRepository.updateLabel(userId, updatedLabel.copy(name = String.random()))
        emit(State.Updated)
        emitAll(observeLabel())
    }.catch {
        emit(State.Error(it.message))
    }

    private fun Flow<DataResult<List<Label>>>.mapState(): Flow<State> = map {
        when (it) {
            is DataResult.Error -> State.Error(it.message)
            is DataResult.Processing -> State.Loading
            is DataResult.Success -> {
                State.Success(
                    rawLabel = it.value
                        .filter { cur -> cur.labelId == labelId }
                        .prettyPrint().truncateToLength(1000).toString(),
                )
            }
        }
    }

    companion object {
        const val ARG_USER_ID = "arg.userId"
        const val ARG_LABEL_ID = "arg.labelId"
        const val ARG_LABEL_TYPE = "arg.labelType"
    }
}
