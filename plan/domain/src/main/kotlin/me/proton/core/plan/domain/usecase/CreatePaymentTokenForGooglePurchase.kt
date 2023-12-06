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

package me.proton.core.plan.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

public interface CreatePaymentTokenForGooglePurchase {
    public suspend operator fun invoke(
        cycle: Int,
        googleProductId: ProductId,
        plan: DynamicPlan,
        purchase: GooglePurchase,
        userId: UserId?
    ): Result

    @ExcludeFromCoverage
    public data class Result(
        public val amount: Long,
        public val cycle: SubscriptionCycle,
        public val currency: Currency,
        public val planNames: List<String>,
        public val token: ProtonPaymentToken
    )
}
