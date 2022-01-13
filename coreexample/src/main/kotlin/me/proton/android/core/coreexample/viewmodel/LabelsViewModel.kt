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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.arch.mapSuccessValueOrNull
import me.proton.core.label.domain.entity.Label
import me.proton.core.label.domain.entity.LabelType
import me.proton.core.label.domain.repository.LabelRepository
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@HiltViewModel
class LabelsViewModel @Inject constructor(
    private val labelRepository: LabelRepository,
    private val accountManager: AccountManager,
) : ViewModel() {

    private val mutableState = MutableStateFlow<State?>(null)
    val state = mutableState.asStateFlow().filterNotNull()

    init {
        viewModelScope.launch { observePrimaryAccountLabels() }
    }

    private suspend fun observePrimaryAccountLabels() {
        accountManager.getPrimaryUserId().filterNotNull().collect { userId ->
            combine(
                labelRepository.observeLabels(userId, LabelType.MessageLabel).mapSuccessValueOrNull(),
                labelRepository.observeLabels(userId, LabelType.MessageFolder).mapSuccessValueOrNull(),
                labelRepository.observeLabels(userId, LabelType.ContactGroup).mapSuccessValueOrNull()
            ) { labels, folders, groups ->
                listOf(labels ?: emptyList(), folders ?: emptyList(), groups ?: emptyList()).flatten()
            }.catch {
                handleDataResultError(it)
            }.collect { result ->
                mutableState.value = State.Labels(result)
            }
        }
    }

    private fun handleDataResultError(error: Throwable) {
        val errorMessage = error.message ?: "Unknown error"
        val errorCause = error.cause ?: Throwable(errorMessage)
        CoreLogger.e("label", errorCause, errorMessage)
        mutableState.value = State.Error(errorMessage)
    }

    sealed class State {
        object Processing : State()
        data class Labels(val labels: List<Label>) : State()
        data class Error(val reason: String) : State()
    }
}
