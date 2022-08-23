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

package me.proton.core.payment.presentation

import me.proton.core.payment.domain.usecase.GetAvailablePaymentProviders
import me.proton.core.payment.domain.usecase.PaymentProvider
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

public interface ActivePaymentProvider {
    public suspend fun getActivePaymentProvider(): PaymentProvider?
    public fun switchNextPaymentProvider(): PaymentProvider?
    public fun getNextPaymentProviderText(): Int?
}

public class ActivePaymentProviderImpl @Inject constructor(
    private val getAvailablePaymentProviders: GetAvailablePaymentProviders
) : ActivePaymentProvider {

    /**
     * A map of available payment providers and whether one of them is currently active or not.
     */
    private lateinit var currentlyAvailablePaymentProviders: Map<PaymentProvider, Boolean>

    private suspend fun init() {
        if (::currentlyAvailablePaymentProviders.isInitialized) {
            return
        }

        val availablePaymentProviders = getAvailablePaymentProviders(refresh = true).filter {
            // Adding PayPal is currently not supported.
            it != PaymentProvider.PayPal
        }
        currentlyAvailablePaymentProviders = when {
            !availablePaymentProviders.contains(PaymentProvider.GoogleInAppPurchase) ->
                availablePaymentProviders.associateWith {
                    val index = availablePaymentProviders.indexOf(it)
                    index == 0
                }
            else -> availablePaymentProviders.associateWith {
                it == PaymentProvider.GoogleInAppPurchase // this takes priority always
            }
        }
    }

    override suspend fun getActivePaymentProvider(): PaymentProvider? {
        init()
        val currentActivePaymentProvider = currentlyAvailablePaymentProviders.filter { provider -> provider.value }
        return if (currentlyAvailablePaymentProviders.isEmpty() || currentActivePaymentProvider.isEmpty()) null
        else currentActivePaymentProvider.keys.first()
    }

    override fun switchNextPaymentProvider(): PaymentProvider? {
        val indexNextSelectedProvider = nextPaymentProviderIndex()
        currentlyAvailablePaymentProviders = currentlyAvailablePaymentProviders.entries.associate {
            val index = currentlyAvailablePaymentProviders.entries.indexOf(it)
            if (index == indexNextSelectedProvider) {
                it.key to true
            } else {
                it.key to false
            }
        }
        val currentActivePaymentProvider = currentlyAvailablePaymentProviders.filter { provider -> provider.value }
        return if (currentlyAvailablePaymentProviders.isEmpty() || currentActivePaymentProvider.isEmpty()) null
        else currentActivePaymentProvider.keys.first()
    }

    public override fun getNextPaymentProviderText(): Int? {
        val nextIndex = nextPaymentProviderIndex()
        var nextProvider: PaymentProvider? = null
        if (currentlyAvailablePaymentProviders.entries.size > 1) {
            currentlyAvailablePaymentProviders.entries.mapIndexed { index, entry ->
                if (index == nextIndex) {
                    nextProvider = entry.key
                }
            }
        }
        return when (nextProvider) {
            PaymentProvider.GoogleInAppPurchase -> R.string.payment_use_google_pay_instead
            PaymentProvider.CardPayment -> R.string.payment_use_credit_card_instead
            else -> null
        }.exhaustive
    }

    private fun nextPaymentProviderIndex(): Int {
        val selectedProvider = currentlyAvailablePaymentProviders.entries.first { it.value }
        val indexOfSelectedProvider = currentlyAvailablePaymentProviders.entries.indexOf(selectedProvider)
        return if (indexOfSelectedProvider == currentlyAvailablePaymentProviders.size - 1) 0
        else indexOfSelectedProvider + 1
    }
}
