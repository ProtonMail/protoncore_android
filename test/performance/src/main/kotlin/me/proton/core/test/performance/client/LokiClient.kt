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

package me.proton.core.test.performance.client

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import junit.framework.TestCase.fail
import me.proton.core.test.performance.LogcatFilter.Companion.clientPerformanceTag
import me.proton.core.test.performance.MeasurementConfig
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import retrofit2.Retrofit
import java.io.ByteArrayInputStream
import java.security.KeyFactory
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@RequiresApi(Build.VERSION_CODES.O)
public object LokiClient {

    private var lokiEndpoint: String? = MeasurementConfig.lokiEndpoint
    private val retrofit: Retrofit by lazy { createRetrofitClient() }
    private val lokiApi: LokiApi by lazy { retrofit.create(LokiApi::class.java) }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadPrivateKey(): PrivateKey {
        val privateKeyPem = MeasurementConfig.lokiPrivateKey?.trimIndent()
        val privateKeyBytes = Base64.getDecoder().decode(
            privateKeyPem
                ?.replace("-----BEGIN PRIVATE KEY-----", "")
                ?.replace("-----END PRIVATE KEY-----", "")
                ?.replace("\\s".toRegex(), "")
        )
        val keySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
    }


    private fun loadCertificate(): X509Certificate {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificateInput: ByteArrayInputStream? = MeasurementConfig.lokiCertificate?.byteInputStream()
        return certificateFactory.generateCertificate(certificateInput) as X509Certificate
    }

    private fun createKeyStore(privateKey: PrivateKey, certificate: X509Certificate): KeyStore {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
        }
        val alias = "privateKey"

        if (!keyStore.containsAlias(alias)) {
            keyStore.setKeyEntry(alias, privateKey, null, arrayOf(certificate))
            Log.d(clientPerformanceTag, "Certificate added to KeyStore")
        } else {
            Log.d(clientPerformanceTag, "Certificate already exists in KeyStore")
        }
        return keyStore
    }

    private fun createSSLContext(keyStore: KeyStore): Pair<SSLContext, X509TrustManager> {
        val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
            init(keyStore, null)
        }

        val trustedCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("TLS").apply {
            init(keyManagerFactory.keyManagers, trustedCerts, null)
        }

        return sslContext to trustedCerts[0] as X509TrustManager
    }

    private fun createSecureOkHttpClient(sslContext: SSLContext, trustManager: X509TrustManager): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier { _, _ -> true } // Optional: disable hostname verification
        return okHttpClient.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun createRetrofitClient(): Retrofit {
        val privateKey = loadPrivateKey()
        val certificate = loadCertificate()
        val keyStore = createKeyStore(privateKey, certificate)
        val (sslContext, trustManager) = createSSLContext(keyStore)
        val client = createSecureOkHttpClient(sslContext, trustManager)

        return Retrofit.Builder()
            .baseUrl(lokiEndpoint!!)
            .client(client)
            .build()
    }

    internal suspend fun pushLokiEntry(entry: String) {
        val requestBody = entry.toRequestBody("application/json".toMediaType())
        val response = lokiApi.pushLokiEntry(requestBody)
        if (response.isSuccessful) {
            Log.d(clientPerformanceTag, "Successfully pushed Loki entry. Response: $response")
        } else {
            Log.d(clientPerformanceTag, "Failed to push Loki entry with response code: ${response.code()}")
            fail("Failed to push Loki entry with response code: ${response.code()}, " +
                "response body: ${response.body()}. Check your Loki environment variables and retry.")

        }
    }
}
