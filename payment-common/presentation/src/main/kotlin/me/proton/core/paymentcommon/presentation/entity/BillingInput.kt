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

package me.proton.core.paymentcommon.presentation.entity

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import me.proton.core.domain.entity.UserId

/**
 * Holds main billing request input.
 * It could be used for Plan Upgrade of the current logged in (or main user if multi user app). In this case pass the
 * user's sessionId.
 * It could also be used for SignUp and in this case sessionId should be null (there is currently no user).
 *
 * @param userId optional user sessionId (as String) depending if it is Upgrade of already logged in user or new
 * user Sign Up.
 * @param plan one or more plans that the user wants to subscribe to.
 * @param codes use this field for eventual coupon or gift codes.
 */
@Parcelize
public data class BillingInput(
    val userId: String?,
    val existingPlans: List<CurrentSubscribedPlanDetails> = emptyList(),
    val plan: PlanShortDetails,
    val codes: List<String>? = null,
    val paymentMethodId: String?,
) : Parcelable {
    @IgnoredOnParcel
    val user: UserId? = if (userId.isNullOrBlank()) null else UserId(userId)
}


