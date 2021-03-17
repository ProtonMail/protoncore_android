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

package me.proton.core.countries.domain.utils

import me.proton.core.countries.domain.entity.Country

val country1 = Country("1", "first", 1)
val country2 = Country("2", "second", 2)
val country3 = Country("3", "third", 3)
val country4 = Country("4", "fourth", 4)
val country5 = Country("5", "fifth", 5)
val country6 = Country("6", "sixth", 6)
val country7 = Country("7", "seventh", 7)
val country8 = Country("8", "eight", 8)
val country9 = Country("9", "ninth", 9)

val testCountriesExcludingMostUsed =
    listOf(
        country1,
        country2,
        country3,
        country4,
        country5,
        country6,
        country7,
        country8,
        country9
    )
