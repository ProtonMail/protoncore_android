/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.plan.presentation.usecase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.plan.domain.entity.DynamicPlanPrice
import me.proton.core.plan.presentation.LogTag
import me.proton.core.plan.presentation.R
import me.proton.core.plan.presentation.entity.PlanCycle
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

class ComposeAutoRenewText @Inject constructor(
    @ApplicationContext private val context: Context
) {

    operator fun invoke(price: DynamicPlanPrice?, cycle: Int): String? {
        val current = price?.current ?: return null
        val default = price.default
        val currency = price.currency
        val priceWithCurrencyText = current.toDouble().formatCentsPriceDefaultLocale(currency)
        return when (cycle) {
            PlanCycle.MONTHLY.value -> {
                when {
                    default != null && current != default -> context.getString(
                        R.string.plan_welcome_price_message_monthly,
                        default.toDouble().formatCentsPriceDefaultLocale(currency)
                    )

                    else -> context.getString(
                        R.string.plan_auto_renew_message_monthly,
                        priceWithCurrencyText
                    )
                }
            }

            PlanCycle.YEARLY.value -> {
                when {
                    default != null && current != default -> context.getString(
                        R.string.plan_welcome_price_message_annual,
                        default.toDouble().formatCentsPriceDefaultLocale(currency)
                    )

                    else -> context.getString(
                        R.string.plan_auto_renew_message_annual,
                        priceWithCurrencyText
                    )
                }
            }

            PlanCycle.TWO_YEARS.value -> {
                when {
                    default != null && current != default -> context.getString(
                        R.string.plan_welcome_price_message_two_year,
                        default.toDouble().formatCentsPriceDefaultLocale(currency)
                    )

                    else -> context.getString(
                        R.string.plan_auto_renew_message_two_years,
                        priceWithCurrencyText
                    )
                }
            }

            else -> {
                CoreLogger.e(LogTag.PLAN_CYCLE, "Dynamic plan cycle unknown: $cycle")
                context.getString(R.string.plan_welcome_price_message_other_fallback)
            }
        }
    }
}
