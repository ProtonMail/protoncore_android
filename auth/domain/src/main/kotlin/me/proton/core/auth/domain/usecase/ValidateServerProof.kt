/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase

import me.proton.core.auth.domain.LogTag
import me.proton.core.auth.domain.entity.ServerProof
import me.proton.core.auth.domain.exception.InvalidServerAuthenticationException
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

class ValidateServerProof @Inject constructor() {

    /**
     * Throws an [InvalidServerAuthenticationException] with the result of calling [lazyMessage]
     * if the [ServerProof] isn't the one expected.
     */
    operator fun invoke(serverProof: ServerProof, expectedProof: String, lazyMessage: () -> Any) {
        if (serverProof != expectedProof) {
            val message = "Server returned invalid srp proof, ${lazyMessage.invoke()}"
            val exception = InvalidServerAuthenticationException(message)
            CoreLogger.e(LogTag.INVALID_SRP_PROOF, exception)
            throw exception
        }
    }
}
