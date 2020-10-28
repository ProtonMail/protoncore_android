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

package me.proton.core.data.arch

import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.network.domain.ApiResult
import me.proton.core.util.kotlin.exhaustive

fun <T> ApiResult<T>.toDataResponse(): DataResult<T> = when (this) {
    is ApiResult.Success -> DataResult.Success(value, ResponseSource.Remote)
    is ApiResult.Error.Http -> {
        DataResult.Error.Message(
            message = proton?.error ?: message,
            source = ResponseSource.Remote,
            code = proton?.code ?: 0 // 0 means no code is present
        )
    }
    is ApiResult.Error.Parse -> DataResult.Error.Message(cause?.message, ResponseSource.Remote)
    is ApiResult.Error.Connection -> DataResult.Error.Message(cause?.message, ResponseSource.Remote)
}.exhaustive
