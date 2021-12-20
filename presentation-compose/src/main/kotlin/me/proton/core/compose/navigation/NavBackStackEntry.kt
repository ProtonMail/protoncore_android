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

package me.proton.core.compose.navigation

import android.os.Bundle
import androidx.navigation.NavBackStackEntry

fun NavBackStackEntry.requireArguments() =
    requireNotNull(arguments) { "Required arguments bundle was null." }

fun <T> NavBackStackEntry.require(key: String, optionalBundle: Bundle? = null): T =
    requireNotNull(get(key, optionalBundle)) { "Required $key was null." }

@Suppress("UNCHECKED_CAST")
fun <T> NavBackStackEntry.get(key: String, optionalBundle: Bundle? = null): T? {
    val bundleArgs = requireArguments()
    return bundleArgs.get(key) as? T ?: optionalBundle?.get(key) as? T
}
