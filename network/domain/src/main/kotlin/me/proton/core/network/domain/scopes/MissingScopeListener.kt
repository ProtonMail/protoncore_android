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

import me.proton.core.network.domain.client.ClientId

interface MissingScopeListener {

    sealed class MissingScopeResult {
        object Success : MissingScopeResult()
        object Failure : MissingScopeResult()
    }

    /**
     * Called when a Missing Scope is needed for a [ClientId].
     * It should show a password entry dialog and obtain the scope from the API.
     */
    suspend fun onScopeNeeded(
        clientId: ClientId,
        scopes: MissingScopes
    ): MissingScopeResult
}
