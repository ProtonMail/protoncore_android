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

package me.proton.core.test.quark.data

import kotlinx.serialization.Serializable

@Serializable
public enum class Plan(public var planName: String, public var text: String) {
    @Deprecated("This plan is no longer enabled.")
    Professional("pro", "Proton Mail Professional"),

    @Deprecated("This plan is no longer enabled.")
    Visionary("visionary", "Visionary"),

    @Deprecated("This plan is no longer enabled.")
    Plus("plus", "ProtonMail Plus"),

    Free("free", "Free"),
    MailPlus("mail2022", "Mail Plus"),
    Unlimited("bundle2022", "Proton Unlimited"),
    VpnPlus("vpn2022", "VPN Plus"),
    Dev("", "")
}

internal fun randomPaidPlan(): Plan = arrayOf(Plan.MailPlus, Plan.Unlimited).random()

public enum class BillingCycle(
    public val value: String,
    public val monthlyPrice: Double,
    public val yearlyPrice: Number
) {
    Yearly("Pay annually", 4.00, 48.00),
}

public enum class Currency(public val symbol: String, public val code: String) {
    Euro("€", "EUR"),
    USD("$", "USD"),
    CHF("CHF", "CHF")
}
