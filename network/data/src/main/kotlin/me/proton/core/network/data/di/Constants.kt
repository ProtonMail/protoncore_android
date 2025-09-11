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
        // api.[protonmail|protonvpn].ch
        // [verify|verify-api].[protonmail|protonvpn].com
        "drtmcR2kFkM8qJClsuWgUzxgBkePfRCkRpqUesyDmeE=", // Current.
        "YRGlaY0jyJ4Jw2/4M8FIftwbDIQfh8Sdro96CeEel54=", // Hot backup.
        "AfMENBVvOS8MnISprtvyPsjKlPooqh8nMB/pvCrpJpw=", // Cold backup.
        // proton.me
        "CT56BhOTmj5ZIPgb/xD5mH8rY3BLo/MlhP7oPyJUEDo=", // Current.
        "35Dx28/uzN3LeltkCBQ8RHK0tlNSa2kCpCRGNp34Gxc=", // Hot backup
        "qYIukVc63DEITct8sFT7ebIq5qsWmuscaIKeJx+5J5A=", // Cold backup.
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

    /**
     * Alternative DNS over HTTPS services urls. DohProvider will randomly pick from this list for additional resolvers.
     */
    val ALTERNATIVE_DOH_PROVIDERS_URLS = listOf(
        "https://anycast.dns.nextdns.io/dns-query/",
        "https://130.59.31.248/dns-query/",
        "https://dns-doh.dnsforfamily.com/dns-query/",
        "https://94.140.14.14/dns-query/",
        "https://1.1.1.2/dns-query/",
        "https://pluton.plan9-dns.com/dns-query/",
        "https://syd.adfilter.net/dns-query/",
        "https://dnspub.restena.lu/dns-query/",
        "https://blank.dnsforge.de/dns-query/",
        "https://dns.mullvad.net/dns-query/",
        "https://dns.digitale-gesellschaft.ch/dns-query/",
        "https://dns.brahma.world/dns-query/",
        "https://94.140.14.140/dns-query/",
        "https://base.dns.mullvad.net/dns-query/",
        "https://per.adfilter.net/dns-query/",
        "https://adl.adfilter.net/dns-query/",
        "https://family.dns.mullvad.net/dns-query/",
        "https://dns.digitalsize.net/dns-query/",
        "https://unicast.uncensoreddns.org/dns-query/",
        "https://dns.circl.lu/dns-query/",
        "https://dns12.quad9.net/dns-query/",
        "https://all.dns.mullvad.net/dns-query/",
        "https://dns.aa.net.uk/dns-query/",
        "https://doh.libredns.gr/dns-query/",
        "https://extended.dns.mullvad.net/dns-query/",
        "https://dns.njal.la/dns-query/",
        "https://dns9.quad9.net/dns-query/",
        "https://149.112.112.9/dns-query/",
        "https://freedns.controld.com/dns-query/",
        "https://76.76.2.11/dns-query/",
        "https://149.112.112.12/dns-query/",
        "https://dnsforge.de/dns-query/",
        "https://94.140.15.16/dns-query/",
        "https://doq.dns4all.eu/dns-query/",
        "https://ns1.fdn.fr/dns-query/",
        "https://dns10.quad9.net/dns-query/",
        "https://9.9.9.10/dns-query/",
        "https://149.112.112.11/dns-query/",
        "https://doh.ffmuc.net/dns-query/",
        "https://odvr.nic.cz/dns-query/",
        "https://ibksturm.synology.me/dns-query/",
        "https://1.0.0.3/dns-query/",
        "https://adblock.dns.mullvad.net/dns-query/",
        "https://dns-doh-no-safe-search.dnsforfamily.com/dns-query/",
        "https://dns1.dnscrypt.ca/dns-query/",
        "https://cloudflare-dns.com/dns-query/",
        "https://anycast.uncensoreddns.org/dns-query/",
        "https://dns.quad9.net/dns-query/",
    )
}
