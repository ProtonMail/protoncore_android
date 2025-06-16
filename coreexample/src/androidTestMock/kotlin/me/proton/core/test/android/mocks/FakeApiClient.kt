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

package me.proton.core.test.android.mocks

import me.proton.core.network.domain.ApiClient
import kotlin.time.Duration.Companion.seconds

class FakeApiClient : ApiClient {
    var shouldUseDoh: Boolean = false
    override val appVersionHeader: String = "android-mock@1.2.3"
    override val userAgent: String = "Mock/1.2.3"
    override val enableDebugLogging: Boolean = true

    override val callTimeoutSeconds: Long = CALL_TIMEOUT.inWholeSeconds
    override val connectTimeoutSeconds: Long = CONNECT_TIMEOUT.inWholeSeconds
    override val pingTimeoutSeconds: Int = PING_TIMEOUT.inWholeSeconds.toInt()
    override val readTimeoutSeconds: Long = READ_WRITE_TIMEOUT.inWholeSeconds
    override val writeTimeoutSeconds: Long = READ_WRITE_TIMEOUT.inWholeSeconds

    override fun forceUpdate(errorMessage: String) = Unit
    override suspend fun shouldUseDoh(): Boolean = shouldUseDoh

    companion object {
        val CALL_TIMEOUT = 20.seconds
        val CONNECT_TIMEOUT = 5.seconds
        val READ_WRITE_TIMEOUT = 5.seconds
        val PING_TIMEOUT = 5.seconds
    }
}
