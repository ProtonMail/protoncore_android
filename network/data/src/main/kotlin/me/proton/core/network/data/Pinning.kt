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
package me.proton.core.network.data

import com.datatheorem.android.trustkit.config.PublicKeyPin
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

/**
 * Inits given okhttp builder with pinning.
 *
 * @param okBuilder builder to introduce pinning to.
 * @param host host for which pins are added.
 * @param pins list of pins (base64, SHA-256). When empty pinning will be disabled (should be used
 *   only for testing).
 */
internal fun initPinning(okBuilder: OkHttpClient.Builder, host: String, pins: Array<String>) {
    if (pins.isNotEmpty()) {
        val pinner = CertificatePinner.Builder()
            .add("**.$host", *pins.map { "sha256/$it" }.toTypedArray())
            .build()
        okBuilder.certificatePinner(pinner)
    }
}

/**
 * Inits given okhttp builder with leaf SPKI pinning. Accepts certificate chain iff leaf certificate
 * SPKI matches one of the [pins].
 *
 * @param okBuilder builder to introduce pinning to.
 * @param pins list of pins (base64, SHA-256). When empty, pinning will be disabled and default
 *   certificate verification will be used (should be used only for testing).
 */
internal fun initSPKIleafPinning(builder: OkHttpClient.Builder, pins: List<String>) {
    if (pins.isNotEmpty()) {
        val trustManager = LeafSPKIPinningTrustManager(pins)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        builder.sslSocketFactory(sslContext.socketFactory, trustManager)
        builder.hostnameVerifier(HostnameVerifier { _, _ ->
            // Verification is based solely on SPKI pinning of leaf certificate
            true
        })
    }
}

internal class LeafSPKIPinningTrustManager(pinnedSPKIHashes: List<String>) : X509TrustManager {

    private val pins: List<PublicKeyPin> = pinnedSPKIHashes.map { PublicKeyPin(it) }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        if (PublicKeyPin(chain.first()) !in pins)
            throw CertificateException("Pin verification failed")
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
        throw CertificateException("Client certificates not supported!")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate?>? = arrayOfNulls(0)
}
