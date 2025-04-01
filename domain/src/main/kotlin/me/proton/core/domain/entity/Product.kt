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

package me.proton.core.domain.entity

enum class Product {
    Calendar,
    Drive,
    Mail,
    Vpn,
    Pass
}

fun Product.clientId(): String = when (this) {
    Product.Calendar -> "android-calendar"
    Product.Drive -> "android-drive"
    Product.Mail -> "android-mail"
    Product.Pass -> "android-pass"
    Product.Vpn -> "android-vpn"
}

fun Product.displayName(): String = when (this) {
    Product.Calendar -> "Proton Calendar"
    Product.Drive -> "Proton Drive"
    Product.Mail -> "Proton Mail"
    Product.Pass -> "Proton Pass"
    Product.Vpn -> "Proton VPN"
}
