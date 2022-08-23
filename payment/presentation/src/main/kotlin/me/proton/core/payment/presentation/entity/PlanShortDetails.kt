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

package me.proton.core.payment.presentation.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.SubscriptionCycle

/**
 * @param vendorNames Map of plan names for app vendors (the plan names are for the given [subscriptionCycle]).
 */
@Parcelize
public data class PlanShortDetails(
    val name: String,
    val displayName: String,
    val subscriptionCycle: SubscriptionCycle,
    val amount: Long? = null,
    val currency: Currency = Currency.EUR,
    val services: Int,
    val type: Int,
    val vendorNames: Map<AppStore, String>
) : Parcelable
