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
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.repository.BillingClientError
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

public interface PerformGiapPurchase<A : Any> {
    /** Performs full GIAP flow. */
    public suspend operator fun invoke(
        activity: A,
        cycle: Int,
        plan: DynamicPlan,
        userId: UserId?
    ): Result

    @ExcludeFromCoverage
    public sealed class Result {
        public sealed class Error : Result() {
            /** The customer ID (`obfuscatedAccountId`) that was received from Google Billing library was empty.
             * It's likely that there is a fake Google Play installed on user's device.
             */
            public object EmptyCustomerId : Error()

            /** An unredeemed [googlePurchase] was detected, which should be resolved. */
            public data class GiapUnredeemed(
                public val cycle: Int,
                public val googleProductId: ProductId,
                public val googlePurchase: GooglePurchase,
                public val plan: DynamicPlan,
            ) : Error()

            /** A product was not found in Google Play. */
            public object GoogleProductDetailsNotFound : Error()

            /** The user has launched the Google Play billing flow UI,
             * but the expected [GooglePurchase] object was not found/received.
             */
            public object PurchaseNotFound : Error()

            /** An error was encountered when calling the Google Play Billing library.
             * It can likely be recovered by retrying.
             * [Handle BillingResult response codes](https://developer.android.com/google/play/billing/errors)
             */
            public data class RecoverableBillingError(public val error: BillingClientError) :
                Error()

            /** An error was encountered when calling the Google Play Billing library.
             * Those errors can't be recovered using retry logic.
             * [Handle BillingResult response codes](https://developer.android.com/google/play/billing/errors)
             */
            public data class UnrecoverableBillingError(public val error: BillingClientError) :
                Error()

            /** The user has cancelled the billing flow. */
            public object UserCancelled : Error()
        }

        public data class GiapSuccess(
            public val purchase: GooglePurchase,
            public val amount: Long,
            public val currency: String,
            public val token: ProtonPaymentToken
        ) : Result()
    }
}
