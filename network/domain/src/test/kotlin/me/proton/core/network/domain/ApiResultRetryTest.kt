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

package me.proton.core.network.domain

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

internal class ApiResultRetryTest {

    @Test
    fun `success result`() {
        val result = ApiResult.Success("")
        assertFalse { result.isRetryable() }
        assertFalse { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `connection error`() {
        val result = ApiResult.Error.Connection()
        assertTrue { result.isRetryable() }
        assertTrue { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `certificate error`() {
        val result = ApiResult.Error.Certificate(Throwable())
        assertFalse { result.isRetryable() }
        assertFalse { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `parse error`() {
        val result = ApiResult.Error.Parse(null)
        assertFalse { result.isRetryable() }
        assertFalse { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `400 error`() {
        val result = ApiResult.Error.Http(400, "Bad Request")
        assertFalse { result.isRetryable() }
        assertFalse { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `408 error`() {
        val result = ApiResult.Error.Http(408, "Request Timeout")
        assertTrue { result.isRetryable() }
        assertTrue { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `429 error with no Retry-After`() {
        val result = ApiResult.Error.Http(429, "Too Many Requests")
        assertTrue { result.isRetryable() }
        assertFalse { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `429 error with small Retry-After `() {
        val result = ApiResult.Error.Http(429, "Too Many Requests", retryAfter = 2.seconds)
        assertTrue { result.isRetryable() }
        assertTrue { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `429 error with big Retry-After`() {
        val result = ApiResult.Error.Http(429, "Too Many Requests", retryAfter = 20.seconds)
        assertTrue { result.isRetryable() }
        assertFalse { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `500 error without Retry-After`() {
        val result = ApiResult.Error.Http(500, "Internal Server Error")
        assertTrue { result.isRetryable() }
        assertTrue { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `500 error with small Retry-After`() {
        val result = ApiResult.Error.Http(500, "Internal Server Error", retryAfter = 2.seconds)
        assertTrue { result.isRetryable() }
        assertTrue { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `500 error with big Retry-After`() {
        val result = ApiResult.Error.Http(500, "Internal Server Error", retryAfter = 20.seconds)
        assertTrue { result.isRetryable() }
        assertFalse { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `503 error without Retry-After`() {
        val result = ApiResult.Error.Http(503, "Service Unavailable")
        assertTrue { result.isRetryable() }
        assertFalse { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `503 error with small Retry-After `() {
        val result = ApiResult.Error.Http(503, "Service Unavailable", retryAfter = 2.seconds)
        assertTrue { result.isRetryable() }
        assertTrue { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    @Test
    fun `503 error with big Retry-After`() {
        val result = ApiResult.Error.Http(503, "Service Unavailable", retryAfter = 20.seconds)
        assertTrue { result.isRetryable() }
        assertFalse { result.needsRetry(0, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
        assertFalse { result.needsRetry(1, MAX_RETRY_COUNT, MAX_RETRY_AFTER) }
    }

    companion object {
        private val MAX_RETRY_AFTER = 10.seconds
        private const val MAX_RETRY_COUNT = 1
    }
}
