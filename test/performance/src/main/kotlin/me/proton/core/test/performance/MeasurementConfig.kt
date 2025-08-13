/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.test.performance

public object MeasurementConfig {

    internal var buildCommitSha1: String = BuildConfig.BUILD_COMMIT_SHA1
        private set

    internal var runId: String = BuildConfig.CI_RUN_ID
        private set

    internal var environment: String = "unknown"
        private set

    internal var lokiEndpoint: String? = null
        private set

    internal var lokiPrivateKey: String? = null
        private set

    internal var lokiCertificate: String? = null
        private set

    public fun setBuildCommitShortSha(value: String): MeasurementConfig = apply {
        buildCommitSha1 = value
    }

    public fun setEnvironment(value: String): MeasurementConfig = apply {
        environment = value
    }

    public fun setCIRunId(value: String): MeasurementConfig = apply {
        runId = value
    }

    public fun setLokiEndpoint(value: String): MeasurementConfig = apply {
        lokiEndpoint = value
    }

    public fun setLokiPrivateKey(value: String?): MeasurementConfig = apply {
        lokiPrivateKey = value
    }

    public fun setLokiCertificate(value: String?): MeasurementConfig = apply {
        lokiCertificate = value
    }
}