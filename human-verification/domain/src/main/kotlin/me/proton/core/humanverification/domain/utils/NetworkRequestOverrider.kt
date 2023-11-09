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

package me.proton.core.humanverification.domain.utils

import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage
import java.io.InputStream

@ExcludeFromCoverage
interface NetworkRequestOverrider {
    fun overrideRequest(
        url: String,
        method: String,
        headers: List<Pair<String, String>>,
        acceptSelfSignedCertificates: Boolean = false,
        body: InputStream? = null,
        bodyType: String? = null,
    ): Result

    data class Result(
        val mimeType: String?,
        val encoding: String?,
        val contents: InputStream?,
        val httpStatusCode: Int,
        val reasonPhrase: String,
        val responseHeaders: Map<String, String>,
    )
}
