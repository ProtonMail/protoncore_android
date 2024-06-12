/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.android.core.coreexample.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.featureflag.domain.entity.FeatureFlag
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
class FeatureFlagsViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val featureFlagRepository: FeatureFlagRepository
) : ProtonViewModel() {
    private val actions = MutableSharedFlow<Action>(replay = 4)

    val state: StateFlow<State> = actions
        .transform { action ->
            val flow = when (action) {
                Action.Load -> onLoad()
            }
            emitAll(flow)
        }.catch {
            emit(State(isError = true))
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), State())

    private fun onLoad() = flow {
        emit(State(isLoading = true))
        val userId = accountManager.getPrimaryUserId().first()
        val flags = featureFlagRepository.getAll(userId).sortedBy { it.featureId.id }
        emit(State(featureFlags = flags))
    }

    suspend fun perform(action: Action) {
        actions.emit(action)
    }

    sealed class Action {
        data object Load : Action()
    }

    data class State(
        val isError: Boolean = false,
        val isLoading: Boolean = false,
        val featureFlags: List<FeatureFlag> = emptyList()
    )
}
