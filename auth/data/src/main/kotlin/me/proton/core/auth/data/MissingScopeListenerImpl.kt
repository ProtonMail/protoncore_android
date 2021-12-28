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

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.network.domain.scopes.MissingScopeResult
import me.proton.core.network.domain.scopes.MissingScopeState
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Singleton

@Singleton
class MissingScopeListenerImpl : MissingScopeListener {
    private val _state = MutableSharedFlow<MissingScopeState>(extraBufferCapacity = 1)

    override val state: SharedFlow<MissingScopeState>
        get() = _state

    override suspend fun onMissingScope(scopes: List<Scope>): MissingScopeResult {
        scopes.forEach {
            when (it) {
                Scope.PASSWORD -> _state.tryEmit(MissingScopeState.PasswordScopeMissing)
                Scope.LOCKED -> _state.tryEmit(MissingScopeState.LockedScopeMissing)
            }.exhaustive
        }
        val state = _state.filter {
            it in listOf(MissingScopeState.ScopeObtainSuccess, MissingScopeState.ScopeObtainFailed)
        }.map {
            when (it) {
                MissingScopeState.ScopeObtainFailed -> MissingScopeResult.Failure
                MissingScopeState.ScopeObtainSuccess -> MissingScopeResult.Success
                else -> MissingScopeResult.Failure
            }.exhaustive
        }.first()
        return state
    }

    override suspend fun onMissingScopeSuccess() {
        _state.tryEmit(MissingScopeState.ScopeObtainSuccess)
    }

    override suspend fun onMissingScopeFailure() {
        _state.tryEmit(MissingScopeState.ScopeObtainFailed)
    }
}
