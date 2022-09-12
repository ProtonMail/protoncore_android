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

import android.annotation.SuppressLint
import android.util.Base64
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
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
fun initSPKIleafPinning(builder: OkHttpClient.Builder, pins: List<String>) {
    if (pins.isNotEmpty()) {
        val trustManager = LeafSPKIPinningTrustManager(pins)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)
        builder.sslSocketFactory(sslContext.socketFactory, trustManager)
        builder.hostnameVerifier { _, _ ->
            // Verification is based solely on SPKI pinning of leaf certificate
            true
        }
    }
}

@SuppressLint("CustomX509TrustManager")
class LeafSPKIPinningTrustManager(pinnedSPKIHashes: List<String>) : X509TrustManager {

    private val pins: List<PublicKeyPin> = pinnedSPKIHashes.map { PublicKeyPin.fromSha256HashBase64(it) }

    @Throws(CertificateException::class)
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        if (PublicKeyPin.fromCertificate(chain.first()) !in pins)
            throw CertificateException("Pin verification failed")
    }

    @Throws(CertificateException::class)
    override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {
        throw CertificateException("Client certificates not supported!")
    }

    override fun getAcceptedIssuers(): Array<X509Certificate?>? = arrayOfNulls(0)
}

/**
 * SHA-256 hash of the certificate's Subject Public Key Info,
 * as described in the HPKP RFC https://tools.ietf.org/html/rfc7469s.
 */
data class PublicKeyPin(
    private val sha256Hash: ByteArray
) {
    override fun equals(other: Any?): Boolean =
        this === other || other is PublicKeyPin && sha256Hash.contentEquals(other.sha256Hash)

    override fun hashCode(): Int = sha256Hash.contentHashCode()

    companion object {

        fun fromCertificate(certificate: Certificate): PublicKeyPin {
            val digest = MessageDigest.getInstance("SHA-256").apply { reset() }
            val sha256Hash = digest.digest(certificate.publicKey.encoded)
            return PublicKeyPin(sha256Hash)
        }

        fun fromSha256HashBase64(sha256HashBase64: String): PublicKeyPin {
            val sha256Hash = Base64.decode(sha256HashBase64, Base64.DEFAULT)
            require(sha256Hash.size == 32) { "Invalid pin: length is not 32 bytes" }
            return PublicKeyPin(sha256Hash)
        }
    }
}
