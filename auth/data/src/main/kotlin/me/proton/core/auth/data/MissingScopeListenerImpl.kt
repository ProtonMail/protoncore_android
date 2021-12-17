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

package me.proton.core.auth.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.scopes.MissingScopeResult
import me.proton.core.network.domain.scopes.MissingScopeState
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.util.kotlin.exhaustive

class MissingScopeListenerImpl : MissingScopeListener {
    private val _stateFlow = MutableStateFlow(MissingScopeState.Default)

    override val stateFlow: MutableStateFlow<MissingScopeState>
        get() = _stateFlow

    override suspend fun onMissingScope(scope: Scope): MissingScopeResult {
        val missingScope = MissingScopeState.MissingScopeNeeded
        missingScope.missingScope = scope
        _stateFlow.tryEmit(missingScope)
        val state = _stateFlow.filter {
            it in listOf(MissingScopeState.MissingScopeSuccess, MissingScopeState.MissingScopeFailed)
        }.map {
            when (it) {
                MissingScopeState.MissingScopeFailed -> MissingScopeResult.Failure
                MissingScopeState.MissingScopeSuccess -> MissingScopeResult.Success
                else -> MissingScopeResult.Failure
            }.exhaustive
        }.first()
        return state
    }

    override suspend fun onMissingScopeSuccess() {
        _stateFlow.tryEmit(MissingScopeState.MissingScopeSuccess)
    }

    override suspend fun onMissingScopeFailure() {
        _stateFlow.tryEmit(MissingScopeState.MissingScopeFailed)
    }
}
