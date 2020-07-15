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
package me.proton.core.network.data.util

import me.proton.core.network.data.initPinning
import me.proton.core.network.data.initSPKIleafPinning
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.File
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun MockWebServer.prepareResponse(code: Int, body: String = "") {
    val response = MockResponse()
        .setResponseCode(code)
        .setBody(body)
    enqueue(response)
}

class TestTLSHelper {

    lateinit var trustManagers: Array<TrustManager>
    lateinit var sslContext: SSLContext

    init {
        File("./src/test/resources/test.jks").inputStream().use { jksInput ->
            val pass = "test00".toCharArray()
            val serverKeyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            serverKeyStore.load(jksInput, pass)

            val algorithm = KeyManagerFactory.getDefaultAlgorithm()
            val keyManagerFactory = KeyManagerFactory.getInstance(algorithm)
            keyManagerFactory.init(serverKeyStore, pass)

            val trustManagerFactory = TrustManagerFactory.getInstance(algorithm)
            trustManagerFactory.init(serverKeyStore)

            trustManagers = trustManagerFactory.trustManagers
            sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagerFactory.keyManagers, trustManagers, null)
        }
    }

    fun createMockServer() = MockWebServer().apply {
        useHttps(sslContext.socketFactory, false)
    }

    fun initPinning(builder: OkHttpClient.Builder, pins: Array<String>) {
        builder.sslSocketFactory(sslContext.socketFactory, trustManagers.first() as X509TrustManager)
        initPinning(builder, "localhost", pins)
    }

    fun setupSPKIleafPinning(builder: OkHttpClient.Builder, pins: List<String>) {
        builder.sslSocketFactory(sslContext.socketFactory, trustManagers.first() as X509TrustManager)
        initSPKIleafPinning(builder, pins)
    }

    companion object {
        val TEST_PINS = arrayOf("sha256/d/dc7p/QKB+PnyVi/JOHUQxrZpfGc2LEdq43JGGii4k=")
        val BAD_PINS  = arrayOf("sha256/a/dc7p/QKB+PnyVi/JOHUQxrZpfGc2LEdq43JGGii4k=")
    }
}
