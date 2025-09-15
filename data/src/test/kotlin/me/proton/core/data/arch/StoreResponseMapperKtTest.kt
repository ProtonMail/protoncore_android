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

import io.mockk.mockk
import me.proton.core.domain.arch.DataResult
import me.proton.core.domain.arch.ResponseSource
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreReadResponseOrigin
import kotlin.test.Test
import kotlin.test.assertEquals

class StoreResponseMapperKtTest {
    @Test
    fun toSuccessDataResult() {
        assertEquals(
            DataResult.Success(ResponseSource.Local, "body"),
            StoreReadResponse.Data("body", StoreReadResponseOrigin.Cache).toDataResult()
        )
        assertEquals(
            DataResult.Success(ResponseSource.Remote, "body"),
            StoreReadResponse.Data("body", StoreReadResponseOrigin.Fetcher()).toDataResult()
        )
        assertEquals(
            DataResult.Success(ResponseSource.Local, "body"),
            StoreReadResponse.Data("body", StoreReadResponseOrigin.SourceOfTruth).toDataResult()
        )
    }

    @Test
    fun toErrorDataResult() {
        val cause = Throwable("cause")
        assertEquals(
            DataResult.Error.Local("cause", cause),
            StoreReadResponse.Error.Exception(cause, StoreReadResponseOrigin.Cache).toDataResult()
        )

        assertEquals(
            DataResult.Error.Remote("msg", null),
            StoreReadResponse.Error.Message("msg", StoreReadResponseOrigin.Fetcher()).toDataResult()
        )

        assertEquals(
            DataResult.Error.Local("msg", null),
            StoreReadResponse.Error.Message("msg", StoreReadResponseOrigin.SourceOfTruth).toDataResult()
        )
    }

    @Test
    fun toProcessingDataResult() {
        assertEquals(
            DataResult.Processing(ResponseSource.Local),
            StoreReadResponse.Loading(StoreReadResponseOrigin.Cache).toDataResult()
        )

        assertEquals(
            DataResult.Processing(ResponseSource.Local),
            StoreReadResponse.Loading(StoreReadResponseOrigin.SourceOfTruth).toDataResult()
        )

        assertEquals(
            DataResult.Processing(ResponseSource.Remote),
            StoreReadResponse.Loading(StoreReadResponseOrigin.Fetcher()).toDataResult()
        )

        assertEquals(
            DataResult.Processing(ResponseSource.Remote),
            StoreReadResponse.NoNewData(mockk()).toDataResult()
        )
    }
}
