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

package me.proton.core.plan.presentation.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import org.jetbrains.annotations.VisibleForTesting
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class ObserveUserCurrency @Inject constructor(
    private val userManager: UserManager
) {
    @VisibleForTesting
    internal val localCurrency = Currency.getInstance(Locale.getDefault()).currencyCode

    @VisibleForTesting
    internal val defaultCurrency = availableCurrencies.firstOrNull { it == localCurrency } ?: fallbackCurrency

    operator fun invoke(userId: UserId?): Flow<String> = when (userId) {
        null -> flowOf(defaultCurrency)
        else -> userManager.observeUser(userId).mapLatest { user -> user?.currency.validate() ?: defaultCurrency }
    }

    private fun String?.validate(): String? = takeIf { it in availableCurrencies }

    companion object {
        // ISO 4217 3-letter codes.
        const val fallbackCurrency = "USD"
        val availableCurrencies = listOf("CHF", "EUR", fallbackCurrency)
    }
}
