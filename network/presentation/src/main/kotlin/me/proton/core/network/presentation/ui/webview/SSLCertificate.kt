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

package me.proton.core.network.presentation.ui.webview

import android.net.http.SslCertificate
import android.os.Build
import me.proton.core.network.data.LeafSPKIPinningTrustManager
import me.proton.core.network.data.di.Constants
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

public fun SslCertificate.isTrustedByLeafSPKIPinning(): Boolean =
    getCompatX509Cert()?.isTrustedByLeafSPKIPinning() ?: false

public fun X509Certificate.isTrustedByLeafSPKIPinning(): Boolean =
    LeafSPKIPinningTrustManager(Constants.ALTERNATIVE_API_SPKI_PINS).runCatching {
        checkServerTrusted(arrayOf(this@isTrustedByLeafSPKIPinning), "generic")
    }.isSuccess

public fun SslCertificate.getCompatX509Cert(): X509Certificate? = when (Build.VERSION.SDK_INT) {
    in Build.VERSION_CODES.Q..Int.MAX_VALUE -> x509Certificate
    else -> {
        // Hidden API, there is no way to access this value otherwise.
        SslCertificate.saveState(this).getByteArray("x509-certificate")?.runCatching {
            CertificateFactory.getInstance("X.509").generateCertificate(inputStream())
        }?.getOrNull() as? X509Certificate
    }
}
