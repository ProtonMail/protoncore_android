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

package me.proton.core.observability.domain.metrics

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import me.proton.core.observability.domain.entity.SchemaId
import me.proton.core.observability.domain.metrics.common.GiapStatus

@Serializable
@Schema(description = "Result of purchasing a product via Google Billing library.")
@SchemaId("https://proton.me/android_core_checkout_giapBilling_purchase_total_v3.schema.json")
public data class CheckoutGiapBillingPurchaseTotal(
    override val Labels: LabelsData,
    @Required override val Value: Long = 1
) : ObservabilityData() {
    public constructor(status: PurchaseStatus) : this(LabelsData(status))

    @Serializable
    public data class LabelsData constructor(
        val status: PurchaseStatus
    )

    @Suppress("EnumEntryName", "EnumNaming")
    public enum class PurchaseStatus {
        success,
        billingUnavailable,
        serviceDisconnected,
        serviceTimeout,
        serviceUnavailable,
        developerError,
        featureNotSupported,
        googlePlayError,
        incorrectCustomerId,
        itemAlreadyOwned,
        itemNotOwned,
        itemUnavailable,
        userCanceled,
        notFound,
        statusNull,
        cancellation,
        unknown
    }
}

public fun GiapStatus.toPurchaseGiapStatus(): CheckoutGiapBillingPurchaseTotal.PurchaseStatus =
    when (this) {
        GiapStatus.success -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.success
        GiapStatus.billingUnavailable -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.billingUnavailable
        GiapStatus.serviceDisconnected -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.serviceDisconnected
        GiapStatus.serviceTimeout -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.serviceTimeout
        GiapStatus.serviceUnavailable -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.serviceUnavailable
        GiapStatus.developerError -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.developerError
        GiapStatus.featureNotSupported -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.featureNotSupported
        GiapStatus.googlePlayError -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.googlePlayError
        GiapStatus.itemAlreadyOwned -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.itemAlreadyOwned
        GiapStatus.itemNotOwned -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.itemNotOwned
        GiapStatus.itemUnavailable -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.itemUnavailable
        GiapStatus.userCanceled -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.userCanceled
        GiapStatus.notFound -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.notFound
        GiapStatus.statusNull -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.statusNull
        GiapStatus.cancellation -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.cancellation
        GiapStatus.unknown -> CheckoutGiapBillingPurchaseTotal.PurchaseStatus.unknown
    }
