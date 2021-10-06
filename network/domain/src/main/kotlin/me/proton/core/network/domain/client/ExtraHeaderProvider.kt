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

package me.proton.core.network.domain.client

/**
 * Allows clients to provide a list of headers to be included in all requests to the API.
 */
interface ExtraHeaderProvider {

    /** List of headers in a (Key, Value) format. */
    val headers: List<Pair<String, String>>
    /** Adds the provided [headers] to the list of extra headers. */
    fun addHeaders(vararg headers: Pair<String, String>)
    /** Removes the first header from the list with this [key]. */
    fun removeFirst(key: String)
    /** Removes all headers that have this [key]. */
    fun removeAll(key: String)
    /** Removes all headers. */
    fun clear()

}
