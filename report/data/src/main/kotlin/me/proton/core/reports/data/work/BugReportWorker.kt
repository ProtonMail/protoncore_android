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

package me.proton.core.reports.data.work

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportExtra
import me.proton.core.report.domain.entity.BugReportMeta
import me.proton.core.report.domain.repository.ReportRepository
import me.proton.core.util.kotlin.deserialize
import me.proton.core.util.kotlin.serialize

@HiltWorker
internal class BugReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reportRepository: ReportRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val bugReport = requireNotNull(inputData.getString(INPUT_BUG_REPORT)?.deserialize<BugReport>())
        val bugReportMeta = requireNotNull(inputData.getString(INPUT_BUG_REPORT_META)?.deserialize<BugReportMeta>())
        val bugReportExtra = inputData.getString(INPUT_BUG_REPORT_EXTRA)?.deserialize<BugReportExtra>()

        return reportRepository.runCatching {
            sendReport(bugReport, bugReportMeta, bugReportExtra)
            Result.success()
        }.recover {
            if ((it as? ApiException)?.isRetryable() == true) {
                Result.retry()
            } else {
                Result.failure(workDataOf(OUTPUT_ERROR_MESSAGE to it.message))
            }
        }.getOrThrow()
    }

    companion object {
        const val OUTPUT_ERROR_MESSAGE = "errorMessage"

        private const val INPUT_BUG_REPORT = "bugReport"
        private const val INPUT_BUG_REPORT_EXTRA = "bugReportExtra"
        private const val INPUT_BUG_REPORT_META = "bugReportMeta"
        private const val WORKER_TAG = "bug-report-worker"

        @VisibleForTesting
        internal fun makeData(
            bugReport: BugReport,
            bugReportMeta: BugReportMeta,
            bugReportExtra: BugReportExtra?
        ): Data {
            return workDataOf(
                INPUT_BUG_REPORT to bugReport.serialize(),
                INPUT_BUG_REPORT_EXTRA to bugReportExtra?.serialize(),
                INPUT_BUG_REPORT_META to bugReportMeta.serialize()
            )
        }

        fun makeWorkerRequest(
            bugReport: BugReport,
            meta: BugReportMeta,
            extra: BugReportExtra?
        ): WorkRequest {
            val inputData = makeData(bugReport, meta, extra)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            return OneTimeWorkRequestBuilder<BugReportWorker>()
                .addTag(WORKER_TAG)
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
        }
    }
}
