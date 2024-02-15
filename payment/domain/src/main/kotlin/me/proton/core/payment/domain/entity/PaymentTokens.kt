/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.payment.domain.entity

import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.network.domain.client.ClientId
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.humanverification.HumanVerificationState
import me.proton.core.network.domain.humanverification.VerificationMethod

@JvmInline
public value class GooglePurchaseToken(public val value: String)

@JvmInline
public value class ProtonPaymentToken(public val value: String)

public fun ProtonPaymentToken.humanVerificationDetails(
    clientId: ClientId,
): HumanVerificationDetails = HumanVerificationDetails(
    clientId = clientId,
    verificationMethods = listOf(VerificationMethod.PAYMENT),
    verificationToken = null,
    state = HumanVerificationState.HumanVerificationSuccess,
    tokenType = TokenType.PAYMENT.value,
    tokenCode = value
)
