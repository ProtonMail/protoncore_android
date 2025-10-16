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

package me.proton.core.plan.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

/**
 * Creates new subscription.
 * Authorized. This means that it could only be used for upgrades. Sign ups should be handled through Human Verification
 * Headers with token and token type "payment".
 */
@ExcludeFromCoverage
interface PerformSubscribe {

    suspend operator fun invoke(
        cycle: SubscriptionCycle,
        paymentToken: ProtonPaymentToken?,
        planNames: List<String>,
        userId: UserId
    ): Subscription
}
