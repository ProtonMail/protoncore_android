/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.humanverification.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

@ExcludeFromCoverage
interface HumanVerificationManager : HumanVerificationProvider, HumanVerificationListener {

    /**
     *  Flow of [HumanVerificationDetails] where [HumanVerificationDetails.state] changed.
     *
     * @param initialState if true, initial state for all details will be raised on subscription.
     */
    fun onHumanVerificationStateChanged(initialState: Boolean = false): Flow<HumanVerificationDetails>

    /**
     * Add (insert or update) a [HumanVerificationDetails].
     */
    suspend fun addDetails(details: HumanVerificationDetails)

    /**
     * Clear all [HumanVerificationDetails], by [clientId].
     */
    suspend fun clearDetails(clientId: ClientId)
}

/**
 * Flow of [HumanVerificationDetails] where [HumanVerificationDetails.state] equals [state].
 *
 * @param initialState if true , initial state for all details in this [state] will be raised on subscription.
 */
fun HumanVerificationManager.onHumanVerificationState(
    vararg state: HumanVerificationState,
    initialState: Boolean = true
): Flow<HumanVerificationDetails> = onHumanVerificationStateChanged(initialState).filter { state.contains(it.state) }
