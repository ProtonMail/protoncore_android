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

package me.proton.core.data.arch

import com.dropbox.android.external.store4.ResponseOrigin
import com.dropbox.android.external.store4.StoreResponse
import io.mockk.mockk
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import kotlin.test.Test
import kotlin.test.assertEquals

class StoreResponseMapperKtTest {
    @Test
    fun toSuccessDataResult() {
        assertEquals(
            DataResult.Success(ResponseSource.Local, "body"),
            StoreResponse.Data("body", ResponseOrigin.Cache).toDataResult()
        )
        assertEquals(
            DataResult.Success(ResponseSource.Remote, "body"),
            StoreResponse.Data("body", ResponseOrigin.Fetcher).toDataResult()
        )
        assertEquals(
            DataResult.Success(ResponseSource.Local, "body"),
            StoreResponse.Data("body", ResponseOrigin.SourceOfTruth).toDataResult()
        )
    }

    @Test
    fun toErrorDataResult() {
        val cause = Throwable("cause")
        assertEquals(
            DataResult.Error.Local("cause", cause),
            StoreResponse.Error.Exception(cause, ResponseOrigin.Cache).toDataResult()
        )

        assertEquals(
            DataResult.Error.Remote("msg", null),
            StoreResponse.Error.Message("msg", ResponseOrigin.Fetcher).toDataResult()
        )

        assertEquals(
            DataResult.Error.Local("msg", null),
            StoreResponse.Error.Message("msg", ResponseOrigin.SourceOfTruth).toDataResult()
        )
    }

    @Test
    fun toProcessingDataResult() {
        assertEquals(
            DataResult.Processing(ResponseSource.Local),
            StoreResponse.Loading(ResponseOrigin.Cache).toDataResult()
        )

        assertEquals(
            DataResult.Processing(ResponseSource.Local),
            StoreResponse.Loading(ResponseOrigin.SourceOfTruth).toDataResult()
        )

        assertEquals(
            DataResult.Processing(ResponseSource.Remote),
            StoreResponse.Loading(ResponseOrigin.Fetcher).toDataResult()
        )

        assertEquals(
            DataResult.Processing(ResponseSource.Remote),
            StoreResponse.NoNewData(mockk()).toDataResult()
        )
    }
}
