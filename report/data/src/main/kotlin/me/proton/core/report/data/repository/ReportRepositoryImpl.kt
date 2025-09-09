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

package me.proton.core.report.data.repository

import android.webkit.MimeTypeMap
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess
import me.proton.core.network.domain.onSuccess
import me.proton.core.report.data.api.ReportApi
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportExtra
import me.proton.core.report.domain.entity.BugReportMeta
import me.proton.core.report.domain.provider.BugReportLogProvider
import me.proton.core.report.domain.repository.ReportRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

public class ReportRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val bugReportLogProvider: Optional<BugReportLogProvider>,
) : ReportRepository {
    override suspend fun sendReport(
        bugReport: BugReport,
        meta: BugReportMeta,
        extra: BugReportExtra?
    ) {
        val clientType = when (meta.product) {
            Product.Mail -> 1
            Product.Vpn -> 2
            Product.Calendar -> 3
            Product.Drive -> 4
            Product.Pass -> 5
            Product.Authenticator -> 10
        }
        val logProvider = bugReportLogProvider.getOrNull()
        val logFile = takeIf { bugReport.shouldAttachLog }
            ?.let { logProvider?.getLog() }

        runCatching {
            apiProvider.get<ReportApi>()
                .invoke {
                    sendBugReport(
                        getMultipartBodyBuilder(
                            bugReport = bugReport,
                            meta = meta,
                            clientType = clientType,
                            country = extra?.country,
                            isp = extra?.isp,
                            logFile = logFile,
                        ).build()
                    )
                }
                .onSuccess { check(it.isSuccess()) }
                .valueOrThrow
        }
            .apply { logFile?.let { logProvider?.releaseLog(logFile) } }
            .getOrThrow()
    }

    private fun getMultipartBodyBuilder(
        bugReport: BugReport,
        meta: BugReportMeta,
        clientType: Int,
        country: String?,
        isp: String?,
        logFile: File?,
    ): MultipartBody.Builder = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(name = "OS", value = meta.osName)
        .addFormDataPart(name = "OSVersion", value = meta.osVersion)
        .addFormDataPart(name = "Client", value = meta.clientName)
        .addFormDataPart(name = "ClientVersion", value = meta.appVersionName)
        .addFormDataPart(name = "ClientType",value = "$clientType")
        .addFormDataPart(name = "Title", value = bugReport.title)
        .addFormDataPart(name = "Description", value = bugReport.description)
        .addFormDataPart(name = "Username", value = bugReport.username)
        .addFormDataPart(name = "Email", value = bugReport.email)
        .apply {
            country?.let {
                addFormDataPart(name = "Country", value = country)
            }
            isp?.let {
                addFormDataPart(name = "ISP", value = isp)
            }
            logFile?.takeIf { file -> file.exists() && file.length() > 0 }?.let { file ->
                addFormDataPart(
                    name = ATTACHMENT,
                    filename = file.name,
                    body = file.asRequestBody(file.mimeType?.toMediaTypeOrNull()),
                )
            }
        }

    private val File.mimeType: String? get() = name
        .substringAfterLast('.', "")
        .let { extension ->
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }

    private companion object {
        private const val ATTACHMENT = "Attachment"
    }
}
