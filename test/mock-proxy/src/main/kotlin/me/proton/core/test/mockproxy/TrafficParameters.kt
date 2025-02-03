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

package me.proton.core.test.mockproxy

public enum class MimeType(public val value: String) {
    JSON("application/json"),
    IMAGE_SVG("image/svg+xml"),
    IMAGE_PNG("image/png"),
    OCTET_STREAM("application/octet-stream"),
    MULTIPART_FORM_DATA("multipart/form-data");
}

public enum class LatencyLevel(public val latencyMs: Int) {
    NONE(0),
    LOW(50),
    MODERATE(150),
    HIGH(300),
    VERY_HIGH(600),
    CRITICAL(1000),
    EXTREME(5000),
}

public enum class BandwidthLimit(public val speedKbps: Int) {
    GPRS(56),                // Real-world maximum
    EDGE(236),               // Real-world maximum
    _2G(256),                // Real-world maximum
    _3G(42000),              // Theoretical maximum for HSPA+
    _4G(1000000),            // 1 Gbps = Theoretical maximum
    WIFI(9600000),           // WiFi 6 theoretical maximum (9.6 Gbps)
    BROADBAND(1000000),      // Common high-speed broadband (1 Gbps)
    NONE(Int.MAX_VALUE)                 // Unlimited
}
