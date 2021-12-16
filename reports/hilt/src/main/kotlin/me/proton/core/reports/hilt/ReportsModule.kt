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

package me.proton.core.reports.hilt

import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.domain.entity.Product
import me.proton.core.reports.data.SendBugReportImpl
import me.proton.core.reports.data.repository.ReportsRepositoryImpl
import me.proton.core.reports.domain.entity.BugReportMeta
import me.proton.core.reports.domain.repository.ReportsRepository
import me.proton.core.reports.domain.usecase.SendBugReport

@Module
@InstallIn(SingletonComponent::class)
internal class ReportsModule {
    @Provides
    fun provideBugReportMeta(appUtils: AppUtils, product: Product): BugReportMeta {
        return BugReportMeta(
            appVersionName = appUtils.appVersionName(),
            clientName = appUtils.appName(),
            osName = "Android",
            osVersion = Build.VERSION.RELEASE,
            product
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal interface ReportsBindModule {
    @Binds
    fun provideReportsRepository(repository: ReportsRepositoryImpl): ReportsRepository

    @Binds
    fun provideSendBugReportUseCase(useCase: SendBugReportImpl): SendBugReport
}
