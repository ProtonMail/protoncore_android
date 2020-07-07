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
package me.proton.core.network.data.util

import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.UserData
import me.proton.core.util.kotlin.Logger

class MockUserData : UserData {

    var loggedOut = false

    override var sessionUid: String = "uid"
    override var accessToken: String = "accessToken"
    override var refreshToken: String = "refreshToken"

    override fun forceLogout() {
        loggedOut = true
    }
}

class MockApiClient : ApiClient {

    var forceUpdated = false

    override var shouldUseDoh = true
    override val appVersionHeader = "TestApp_1.0"
    override val userAgent = "Test/1.0 (Android 10; brand model)"

    override val enableDebugLogging: Boolean = true
    override var backoffRetryCount = 2

    override fun forceUpdate() {
        forceUpdated = true
    }
}

class MockNetworkPrefs : NetworkPrefs {
    override var activeAltBaseUrl: String? = null
    override var lastPrimaryApiFail: Long = Long.MIN_VALUE
    override var alternativeBaseUrls: List<String>? = null
}

class MockLogger : Logger {

    override fun e(e: Throwable) {
        e.printStackTrace()
    }
}
