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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.entity.NewLabel
import me.proton.core.label.domain.repository.LabelRepository
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
class CreateLabelViewModel @Inject constructor(
    private val labelRepository: LabelRepository,
    private val accountManager: AccountManager,
) : ViewModel() {

    private val mutableState = MutableStateFlow<State>(State.Idling)
    val state = mutableState.asStateFlow()

    fun createLabel(name: String) {
        mutableState.value = State.Processing
        viewModelScope.launch {
            try {
                val userId = accountManager.getPrimaryUserId().filterNotNull().first()
                labelRepository.createLabel(
                    userId = userId,
                    label = NewLabel(
                        parentId = null,
                        name = name,
                        type = LabelType.MessageLabel,
                        color = "#e7d292",
                        isNotified = false,
                        isExpanded = false,
                        isSticky = false
                    )
                )
                mutableState.value = State.Success
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                CoreLogger.e("label", throwable)
                mutableState.value = State.Error(throwable.message ?: "Unknown error")
            }
            mutableState.value = State.Idling
        }
    }

    sealed class State {
        object Idling : State()
        object Processing : State()
        data class Error(val reason: String) : State()
        object Success : State()
    }
}
