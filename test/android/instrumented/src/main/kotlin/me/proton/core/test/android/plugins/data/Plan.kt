/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.test.android.plugins.data

import kotlinx.serialization.Serializable

@Serializable
enum class Plan(var planName: String, var text: String) {
    Free("free", "Proton Free"),
    Professional("pro", "Proton Mail Professional"),
    Visionary("visionary", "Visionary"),
    Plus("plus", "Mail Plus"),
    Dev("", "")
}

fun randomPaidPlan(): Plan = Plan.values().filter { it != Plan.Free }.random()

enum class BillingCycle(val value: String, val monthlyPrice: Double, val yearlyPrice: Number) {
    Yearly("Pay annually", 4.00, 48.00),
}

enum class Currency(val symbol: String, val code: String) {
    Euro("€", "EUR"),
    USD("$", "USD"),
    CHF("CHF", "CHF")
}
