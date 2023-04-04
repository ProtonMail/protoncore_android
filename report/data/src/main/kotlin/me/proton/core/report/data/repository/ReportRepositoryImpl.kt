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

import me.proton.core.domain.entity.Product
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess
import me.proton.core.network.domain.onSuccess
import me.proton.core.report.data.api.ReportApi
import me.proton.core.report.data.api.request.BugReportRequest
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportExtra
import me.proton.core.report.domain.entity.BugReportMeta
import me.proton.core.report.domain.repository.ReportRepository
import javax.inject.Inject

public class ReportRepositoryImpl @Inject constructor(
    private val apiProvider: ApiProvider,
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
        }

        val request = BugReportRequest(
            osName = meta.osName,
            osVersion = meta.osVersion,
            client = meta.clientName,
            clientType = clientType,
            appVersionName = meta.appVersionName,
            title = bugReport.title,
            description = bugReport.description,
            username = bugReport.username,
            email = bugReport.email,
            country = extra?.country,
            isp = extra?.isp
        )

        apiProvider.get<ReportApi>()
            .invoke { sendBugReport(request) }
            .onSuccess { check(it.isSuccess()) }
            .valueOrThrow
    }
}
