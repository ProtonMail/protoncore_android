/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.network.data.doh

import android.content.Context
import me.proton.core.network.data.R
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.regex.Pattern

fun Context.readSelfSignedDoHCerts(): KeyStore {
    val certificates = resources.openRawResource(R.raw.doh_certs).use { certFile ->
        certFile.readBytes().decodeToString()
            // Split by 'BEGIN CERTIFICATE' without removing it
            .split(Pattern.compile("(?=-----BEGIN CERTIFICATE-----)"))
            .takeWhile { it.isNotEmpty() }
    }
    val keystore = KeyStore.getInstance("BKS")
    keystore.load(null, null)
    val certificateFactory = CertificateFactory.getInstance("X.509")
    certificates.forEachIndexed { index, certContents ->
        val certificate = certContents.byteInputStream().use { certInputStream ->
            runCatching { certificateFactory.generateCertificate(certInputStream) }.getOrNull()
        } ?: return@forEachIndexed

        keystore.setCertificateEntry("ca-$index", certificate)
    }
    return keystore
}
