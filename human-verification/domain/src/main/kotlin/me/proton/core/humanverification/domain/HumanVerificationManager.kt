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

package me.proton.core.humanverification.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState

interface HumanVerificationManager {

    /**
     * Observes the human verification state.
     * @param initialState if true, initial state for all accounts will be raised on subscription.
     */
    fun onHumanVerificationStateChanged(initialState: Boolean = false): Flow<HumanVerificationDetails>
}

/**
 * Flow of HumanVerificationHeaders where [HumanVerificationDetails.state] equals [state].
 *
 * @param initialState if true (default), initial state for all accounts in this [state] will be raised on subscription.
 */
fun HumanVerificationManager.onHumanVerificationState(
    vararg state: HumanVerificationState,
    initialState: Boolean = true
): Flow<HumanVerificationDetails> =
    onHumanVerificationStateChanged(initialState).filter { state.contains(it.state) }
