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

import com.dropbox.android.external.store4.ResponseOrigin
import com.dropbox.android.external.store4.StoreResponse
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import me.proton.core.util.kotlin.exhaustive

fun <T> StoreResponse<T>.toDataResult(): DataResult<T> = when (this) {
    is StoreResponse.Data ->
        when (origin) {
            ResponseOrigin.Fetcher -> DataResult.Success(ResponseSource.Remote, value)
            ResponseOrigin.Cache,
            ResponseOrigin.SourceOfTruth -> DataResult.Success(ResponseSource.Local, value)
        }
    is StoreResponse.Error -> {
        when (origin) {
            ResponseOrigin.Fetcher -> DataResult.Error.Remote(errorMessageOrNull())
            ResponseOrigin.Cache -> DataResult.Error.Local(errorMessageOrNull())
            ResponseOrigin.SourceOfTruth -> DataResult.Error.Local(errorMessageOrNull())
        }
    }
    is StoreResponse.Loading -> when (origin) {
        ResponseOrigin.Fetcher -> DataResult.Processing(ResponseSource.Remote)
        ResponseOrigin.Cache,
        ResponseOrigin.SourceOfTruth -> DataResult.Processing(ResponseSource.Local)
    }
    is StoreResponse.NoNewData -> DataResult.Processing(ResponseSource.Remote)
}.exhaustive
