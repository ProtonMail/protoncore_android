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

package me.proton.core.network.data.client

import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.util.kotlin.removeFirst

class ExtraHeaderProviderImpl: ExtraHeaderProvider {
    private var _headers = mutableListOf<Pair<String, String>>()
    override val headers: List<Pair<String, String>> = _headers

    override fun addHeaders(vararg headers: Pair<String, String>) {
        _headers.addAll(headers)
    }

    override fun removeFirst(key: String) {
        _headers.removeFirst { it.first == key }
    }

    override fun removeAll(key: String) {
        _headers.removeAll { it.first == key }
    }

    override fun clear() = _headers.clear()
}
