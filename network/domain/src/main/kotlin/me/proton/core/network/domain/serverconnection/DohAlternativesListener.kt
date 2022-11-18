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

package me.proton.core.network.domain.serverconnection

interface DohAlternativesListener {

    /**
     * Called when core failed to refresh alt. proxies and gives client a chance to unblock on it's own.
     *
     * @param alternativesBlockCall should be executed once client successfully unblocks API access.
     */
    suspend fun onAlternativesUnblock(alternativesBlockCall: suspend () -> Unit)

    /**
     * Called when core was unable to complete API call through proxies.
     */
    suspend fun onProxiesFailed()
}
