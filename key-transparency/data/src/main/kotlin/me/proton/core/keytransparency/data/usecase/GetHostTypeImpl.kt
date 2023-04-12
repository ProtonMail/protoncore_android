/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.keytransparency.data.usecase

import me.proton.core.keytransparency.domain.KeyTransparencyLogger
import me.proton.core.keytransparency.domain.usecase.GetHostType
import me.proton.core.keytransparency.domain.usecase.HostType
import me.proton.core.network.data.di.BaseProtonApiUrl
import okhttp3.HttpUrl
import javax.inject.Inject

public class GetHostTypeImpl @Inject constructor(
    @BaseProtonApiUrl private val baseUrl: HttpUrl
) : GetHostType {
    public override operator fun invoke(): HostType {
        val host = baseUrl.host
        val prodRegex = ".+\\.proton\\.me".toRegex()
        val devRegex = "[^.]+\\.proton\\.black".toRegex()
        return when {
            host.matches(prodRegex) -> HostType.Production
            host.matches(devRegex) -> HostType.Black
            else -> {
                KeyTransparencyLogger.d("Host does not support KT: $host")
                HostType.Other
            }
        }
    }
}

