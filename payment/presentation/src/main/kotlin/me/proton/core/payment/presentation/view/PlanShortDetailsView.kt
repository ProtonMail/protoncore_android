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

package me.proton.core.payment.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import me.proton.core.payment.presentation.R
import me.proton.core.payment.presentation.databinding.PlanShortDetailsBinding
import me.proton.core.paymentcommon.domain.entity.SubscriptionCycle
import me.proton.core.paymentcommon.presentation.entity.PlanShortDetails
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale

internal class PlanShortDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = PlanShortDetailsBinding.inflate(LayoutInflater.from(context), this)

    init {
        binding.amountProgress.visibility = View.VISIBLE
    }

    var userReadablePlanAmount: String? = null

    var plan: PlanShortDetails? = null
        set(value) = with(binding) {
            val notAvailable = context.getString(R.string.payments_info_not_available)
            planNameText.text = value?.displayName ?: notAvailable
            billingPeriodText.text = when (value?.subscriptionCycle) {
                SubscriptionCycle.YEARLY -> context.getString(R.string.payments_billing_yearly)
                else -> notAvailable
            }
            value?.amount?.let {
                amountProgress.visibility = View.GONE
                userReadablePlanAmount = it.toDouble().formatCentsPriceDefaultLocale(value.currency.name)
                amountText.text = userReadablePlanAmount
            } ?: run {
                amountProgress.visibility = View.VISIBLE
            }
        }
}
