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

package configuration

import configuration.util.getTokenFromCurl

open class EnvironmentConfigSettings : EnvironmentConfig() {
    private var _host: String? = null
    override var host: String
        get() = _host ?: "proton.me"
        set(value) {
            _host = value
        }

    private var _apiPrefix: String? = null
    override var apiPrefix: String
        get() = _apiPrefix ?: "api"
        set(value) {
            _apiPrefix = value
        }

    private var _apiHost: String? = null
    override var apiHost: String
        get() = _apiHost ?: "$apiPrefix.$host"
        set(value) {
            _apiHost = value
        }

    private var _baseUrl: String? = null
    override var baseUrl: String
        get() = _baseUrl ?: "https://$apiHost"
        set(value) {
            _baseUrl = value
        }

    private var _hv3Host: String? = null
    override var hv3Host: String
        get() = _hv3Host ?: "verify.$host"
        set(value) {
            _hv3Host = value
        }

    private var _hv3Url: String? = null
    override var hv3Url: String
        get() = _hv3Url ?: "https://$hv3Host"
        set(value) {
            _hv3Url = value
        }

    private var _useDefaultPins: Boolean? = null
    final override var useDefaultPins: Boolean
        get() = _useDefaultPins ?: true
        private set(value) {
            _useDefaultPins = value
        }

    private var _useProxy: Boolean? = null
    var useProxy: Boolean
        get() = _useProxy ?: false
        set(value) {
            _useProxy = value
        }

    private var _proxyToken: String? = null
    final override var proxyToken: String?
        get() = proxyTokenFromCurl.takeIf { useProxy } ?: ""
        private set(value) {
            _proxyToken = value
        }
}

private val proxyTokenFromCurl: String by lazy {
    val atlasProxyUrl: String? = System.getenv("ATLAS_PROXY_URL")
    if (atlasProxyUrl.isNullOrEmpty()) {
        println("ATLAS_PROXY_URL is not set. skipping proxy token setting")
        return@lazy ""
    }

    getTokenFromCurl(atlasProxyUrl)
}
