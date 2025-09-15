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
import me.proton.core.util.kotlin.exhaustive
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin

fun <T> StoreReadResponse<T>.toDataResult(): DataResult<T> = when (this) {
    is StoreReadResponse.Data ->
        when (origin) {
            is StoreReadResponseOrigin.Fetcher -> DataResult.Success(ResponseSource.Remote, value)
            is StoreReadResponseOrigin.Cache,
            is StoreReadResponseOrigin.SourceOfTruth -> DataResult.Success(ResponseSource.Local, value)
        }
    is StoreReadResponse.Error -> {
        val cause = (this as? StoreReadResponse.Error.Exception)?.error
        when (origin) {
            is StoreReadResponseOrigin.Fetcher -> DataResult.Error.Remote(errorMessageOrNull(), cause)
            is StoreReadResponseOrigin.Cache -> DataResult.Error.Local(errorMessageOrNull(), cause)
            is StoreReadResponseOrigin.SourceOfTruth -> DataResult.Error.Local(errorMessageOrNull(), cause)
        }
    }
    is StoreReadResponse.Loading -> when (origin) {
        is StoreReadResponseOrigin.Fetcher -> DataResult.Processing(ResponseSource.Remote)
        is StoreReadResponseOrigin.Cache,
        is StoreReadResponseOrigin.SourceOfTruth -> DataResult.Processing(ResponseSource.Local)
    }
    is StoreReadResponse.NoNewData -> DataResult.Processing(ResponseSource.Remote)
}.exhaustive
