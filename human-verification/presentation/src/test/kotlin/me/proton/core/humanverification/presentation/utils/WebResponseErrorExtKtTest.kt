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

package me.proton.core.humanverification.presentation.utils

import android.webkit.WebResourceResponse
import io.mockk.every
import io.mockk.mockk
import me.proton.core.humanverification.presentation.ui.common.WebResponseError
import me.proton.core.observability.domain.metrics.HvPageLoadTotal.Status
import java.io.InputStream
import java.io.InputStream.nullInputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class WebResponseErrorExtKtTest {
    @Test
    fun `convert 2xx WebResponseError to observability status`() {
        assertEquals(
            Status.http2xx,
            makeHttpErrorResponse(200).toHvPageLoadStatus()
        )

        assertEquals(
            Status.http2xx,
            makeHttpErrorResponse(201).toHvPageLoadStatus()
        )
    }

    @Test
    fun `convert 4xx WebResponseError to observability status`() {
        assertEquals(
            Status.http400,
            makeHttpErrorResponse(400).toHvPageLoadStatus()
        )

        assertEquals(
            Status.http404,
            makeHttpErrorResponse(404).toHvPageLoadStatus()
        )

        assertEquals(
            Status.http422,
            makeHttpErrorResponse(422).toHvPageLoadStatus()
        )

        assertEquals(
            Status.http4xx,
            makeHttpErrorResponse(403).toHvPageLoadStatus()
        )
    }

    @Test
    fun `convert 5xx WebResponseError to observability status`() {
        assertEquals(
            Status.http5xx,
            makeHttpErrorResponse(500).toHvPageLoadStatus()
        )

        assertEquals(
            Status.http5xx,
            makeHttpErrorResponse(503).toHvPageLoadStatus()
        )
    }

    @Test
    fun `convert unknown http WebResponseError to observability status`() {
        assertEquals(
            Status.connectionError,
            makeHttpErrorResponse(0).toHvPageLoadStatus()
        )
    }

    @Test
    fun `convert ssl WebResponseError to observability status`() {
        assertEquals(
            Status.sslError,
            WebResponseError.Ssl(mockk()).toHvPageLoadStatus()
        )
    }

    @Test
    fun `convert resource WebResponseError to observability status`() {
        assertEquals(
            Status.connectionError,
            WebResponseError.Resource(mockk()).toHvPageLoadStatus()
        )
    }

    @Test
    fun `convert null WebResponseError to observability status`() {
        assertEquals(
            Status.connectionError,
            null.toHvPageLoadStatus()
        )
    }

    private fun makeHttpErrorResponse(statusCode: Int): WebResponseError.Http =
        WebResponseError.Http(
            mockk {
                every { this@mockk.statusCode } returns statusCode
            }
        )
}
