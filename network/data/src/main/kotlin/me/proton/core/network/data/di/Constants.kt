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
package me.proton.core.network.data.di

object Constants {

    /**
     * Certificate pins for Proton API (base64, SHA-256).
     */
    val DEFAULT_SPKI_PINS = arrayOf(
        "drtmcR2kFkM8qJClsuWgUzxgBkePfRCkRpqUesyDmeE=",
        "YRGlaY0jyJ4Jw2/4M8FIftwbDIQfh8Sdro96CeEel54=",
        "AfMENBVvOS8MnISprtvyPsjKlPooqh8nMB/pvCrpJpw="
    )

    /**
     * SPKI pins for alternative Proton API leaf certificates (base64, SHA-256).
     */
    val ALTERNATIVE_API_SPKI_PINS = listOf(
        "EU6TS9MO0L/GsDHvVc9D5fChYLNy5JdGYpJw0ccgetM=",
        "iKPIHPnDNqdkvOnTClQ8zQAIKG0XavaPkcEo0LBAABA=",
        "MSlVrBCdL0hKyczvgYVSRNm88RicyY04Q2y5qrBt0xA=",
        "C2UxW0T1Ckl9s+8cXfjXxlEqwAfPM4HiW2y3UdtBeCw="
    )

    /**
     * DNS over HTTPS services urls.
     */
    val DOH_PROVIDERS_URLS =
        arrayOf("https://dns11.quad9.net/dns-query/", "https://dns.google/dns-query/")
}
