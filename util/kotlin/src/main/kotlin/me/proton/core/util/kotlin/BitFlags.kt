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

package me.proton.core.util.kotlin

/**
 * Determines using bitwise operator if the binary representation of this int contains all the bits of the binary
 * representation of [flag].
 */
fun Int.hasFlag(flag: Int): Boolean = flag and this == flag

/**
 * Determines using bitwise operator if the binary representation of this int matches the bitmask [mask].
 */
fun Int.matchesMask(mask: Int): Boolean = mask or this == mask
