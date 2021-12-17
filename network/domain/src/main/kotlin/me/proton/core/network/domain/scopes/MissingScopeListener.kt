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

package me.proton.core.network.domain.scopes

import kotlinx.coroutines.flow.MutableStateFlow

interface MissingScopeListener {

    val stateFlow: MutableStateFlow<MissingScopeState>

    /**
     * Called when a scope is missing for the user to complete an operation.
     */
    suspend fun onMissingScope(scope: Scope): MissingScopeResult

    /**
     * Called on a missing scope result success.
     */
    suspend fun onMissingScopeSuccess()

    /**
     * Called on a missing scope result failure.
     */
    suspend fun onMissingScopeFailure()
}
