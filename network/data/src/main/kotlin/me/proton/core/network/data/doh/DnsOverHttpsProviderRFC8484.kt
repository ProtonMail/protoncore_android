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
package me.proton.core.network.data.doh

import me.proton.core.network.data.safeApiCall
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.DohService
import me.proton.core.network.domain.NetworkManager
import me.proton.core.util.kotlin.Logger
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Base64
import org.minidns.dnsmessage.DnsMessage
import org.minidns.dnsmessage.Question
import org.minidns.record.Record
import org.minidns.record.TXT
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.TimeUnit

class DnsOverHttpsProviderRFC8484(
    baseOkHttpClient: OkHttpClient,
    private val baseUrl: String,
    private val networkManager: NetworkManager,
    private val logger: Logger
) : DohService {

    private val api: DnsOverHttpsRetrofitApi

    init {
        require(baseUrl.endsWith('/'))

        val converterFactory = object : Converter.Factory() {
            override fun responseBodyConverter(
                type: Type,
                annotations: Array<Annotation>,
                retrofit: Retrofit
            ): Converter<ResponseBody, *>? = Converter<ResponseBody, DnsMessage> { body ->
                body.use {
                    DnsMessage(it.bytes())
                }
            }
        }

        val httpClientBuilder = baseOkHttpClient.newBuilder()
            .connectTimeout(TIMEOUT_S, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_S, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_S, TimeUnit.SECONDS)

        val okClient = httpClientBuilder.build()
        api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okClient)
            .addConverterFactory(converterFactory)
            .build()
            .create(DnsOverHttpsRetrofitApi::class.java)
    }

    override suspend fun getAlternativeBaseUrls(primaryBaseUrl: String): List<String>? {
        val primaryURI = URI(primaryBaseUrl)
        val base32domain = Base32().encodeAsString(primaryURI.host.toByteArray()).trim('=')
        val question = Question("d$base32domain.protonpro.xyz", Record.TYPE.TXT)
        val queryMessage = DnsMessage.builder()
            .setRecursionDesired(true)
            .setQuestion(question)
            .build()
        val queryMessageBase64 = Base64(true).encodeToString(
            queryMessage.toArray())

        val response = safeApiCall(networkManager, logger, api) {
            api.getServers(baseUrl.removeSuffix("/"), queryMessageBase64)
        }
        if (response is ApiResult.Success) {
            val answers = response.value.answerSection
            return try {
                answers
                    .mapNotNull { (it.payload as? TXT)?.text }
                    .map { URI("https", it, primaryURI.path, null).toString() }
                    .takeIf { it.isNotEmpty() }
            } catch (e: URISyntaxException) {
                null
            }
        }
        return null
    }

    companion object {
        private const val TIMEOUT_S = 10L
    }
}
