/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.network.domain.session

import me.proton.core.network.domain.humanverification.HumanVerificationApiDetails
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState.HumanVerificationFailed

interface HumanVerificationListener {

    /**
     * Called when a Human Verification Workflow is needed for a [ClientId].
     *
     * Implementation of this function should suspend until a [HumanVerificationResult] is returned.
     *
     * Any consecutive API call made without an updated and valid [HumanVerificationDetails] will return the same error
     * and then will be queued until this function return. After, queued calls will be retried.
     */
    suspend fun onHumanVerificationNeeded(
        clientId: ClientId,
        details: HumanVerificationApiDetails
    ): HumanVerificationResult

    /**
     * Called when a human verification process has failed for a particular [ClientId]. It means that it got
     * [HumanVerificationFailed] status.
     */
    suspend fun onHumanVerificationFailed(clientId: ClientId)

    /**
     * Called when the human verification ended positive, and now we have the Token Code and Token Type.
     * [HumanVerificationSuccess] is the new status.
     */
    suspend fun onHumanVerificationPassed(clientId: ClientId)

    /**
     * This is a special case (exception), when for SignUp of External Email accounts (user is creating Proton Account
     * with his 3rd party email as username). This is called only during the SignUp process.
     */
    suspend fun onExternalAccountHumanVerificationNeeded(
        clientId: ClientId,
        details: HumanVerificationDetails
    )

    /**
     * Indicating that the Human Verification for External Email accounts (user is creating Proton Account with his 3rd
     * party email as username) has completed.
     */
    suspend fun onExternalAccountHumanVerificationDone()

    sealed class HumanVerificationResult {
        object Success : HumanVerificationResult()
        object Failure : HumanVerificationResult()
    }
}
