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

package me.proton.core.payment.presentation.viewmodel

import me.proton.core.observability.domain.metrics.CheckoutBillingSubscribeTotal
import me.proton.core.observability.domain.metrics.CheckoutBillingSubscribeTotal.Manager.google
import me.proton.core.observability.domain.metrics.CheckoutBillingSubscribeTotal.Manager.proton
import me.proton.core.observability.domain.metrics.CheckoutCardBillingCreatePaymentTokenTotal
import me.proton.core.observability.domain.metrics.CheckoutCardBillingValidatePlanTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingCreatePaymentTokenTotal
import me.proton.core.observability.domain.metrics.CheckoutGiapBillingValidatePlanTotal
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsCreatePaymentTokenTotal
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsSubscribeTotal
import me.proton.core.observability.domain.metrics.CheckoutPaymentMethodsValidatePlanTotal
import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.observability.domain.metrics.common.toHttpApiStatus
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.usecase.PaymentProvider

public fun Result<*>.getCreatePaymentTokenObservabilityData(
    paymentType: PaymentType
): ObservabilityData = when (paymentType) {
    is PaymentType.CreditCard -> CheckoutCardBillingCreatePaymentTokenTotal(this)
    is PaymentType.GoogleIAP -> CheckoutGiapBillingCreatePaymentTokenTotal(this)
    is PaymentType.PaymentMethod -> CheckoutPaymentMethodsCreatePaymentTokenTotal(this)
    is PaymentType.PayPal -> throw NotImplementedError("Paypal not supported.")
}

public fun Result<*>.getSubscribeObservabilityData(
    paymentType: PaymentType
): ObservabilityData = when (paymentType) {
    is PaymentType.CreditCard -> CheckoutBillingSubscribeTotal(toHttpApiStatus(), proton)
    is PaymentType.GoogleIAP -> CheckoutBillingSubscribeTotal(toHttpApiStatus(), google)
    is PaymentType.PaymentMethod -> CheckoutPaymentMethodsSubscribeTotal(toHttpApiStatus())
    is PaymentType.PayPal -> throw NotImplementedError("Paypal not supported.")
}

public fun Result<*>.getValidatePlanObservabilityData(
    paymentType: PaymentType
): ObservabilityData = when (paymentType) {
    is PaymentType.CreditCard -> CheckoutCardBillingValidatePlanTotal(toHttpApiStatus())
    is PaymentType.GoogleIAP -> CheckoutGiapBillingValidatePlanTotal(toHttpApiStatus())
    is PaymentType.PaymentMethod -> CheckoutPaymentMethodsValidatePlanTotal(toHttpApiStatus())
    is PaymentType.PayPal -> throw NotImplementedError("Paypal not supported.")
}

public fun Result<*>.getCreatePaymentTokenObservabilityData(
    paymentProvider: PaymentProvider?
): ObservabilityData = when (paymentProvider) {
    PaymentProvider.CardPayment -> CheckoutCardBillingCreatePaymentTokenTotal(this)
    PaymentProvider.GoogleInAppPurchase -> CheckoutGiapBillingCreatePaymentTokenTotal(this)
    else -> error("Provider is not supported ($paymentProvider).")
}

public fun Result<*>.getSubscribeObservabilityData(
    paymentProvider: PaymentProvider?
): ObservabilityData = when (paymentProvider) {
    PaymentProvider.CardPayment -> CheckoutBillingSubscribeTotal(toHttpApiStatus(), proton)
    PaymentProvider.GoogleInAppPurchase -> CheckoutBillingSubscribeTotal(toHttpApiStatus(), google)
    else -> error("Provider is not supported ($paymentProvider).")
}

public fun Result<*>.getValidatePlanObservabilityData(
    paymentProvider: PaymentProvider?
): ObservabilityData = when (paymentProvider) {
    PaymentProvider.CardPayment -> CheckoutCardBillingValidatePlanTotal(toHttpApiStatus())
    PaymentProvider.GoogleInAppPurchase -> CheckoutGiapBillingValidatePlanTotal(toHttpApiStatus())
    else -> error("Provider is not supported ($paymentProvider).")
}
