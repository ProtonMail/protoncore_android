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

package me.proton.core.paymentiap.domain.entity

import com.android.billingclient.api.ProductDetails
import me.proton.core.payment.domain.entity.GoogleProductDetails

internal class GoogleProductDetailsWrapper(
    val productDetails: ProductDetails
) : GoogleProductDetails

public fun GoogleProductDetails.unwrap(): ProductDetails =
    (this as GoogleProductDetailsWrapper).productDetails

public fun ProductDetails.wrap(): GoogleProductDetails = GoogleProductDetailsWrapper(this)
