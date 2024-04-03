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

package me.proton.core.report.dagger

import android.os.Build
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet
import me.proton.core.domain.entity.Product
import me.proton.core.report.data.SendBugReportImpl
import me.proton.core.report.data.repository.ReportRepositoryImpl
import me.proton.core.report.domain.entity.BugReportMeta
import me.proton.core.report.domain.provider.BugReportLogProvider
import me.proton.core.report.domain.repository.ReportRepository
import me.proton.core.report.domain.usecase.SendBugReport

@Module
@InstallIn(SingletonComponent::class)
internal class CoreReportModule {
    @Provides
    fun provideBugReportMeta(appUtils: AppUtils, product: Product): BugReportMeta {
        return BugReportMeta(
            appVersionName = appUtils.appVersionName(),
            clientName = "Android " + appUtils.appName(),
            osName = "Android",
            osVersion = Build.VERSION.RELEASE,
            product
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal interface CoreReportBindModule {
    @Binds
    fun provideReportRepository(repository: ReportRepositoryImpl): ReportRepository

    @Binds
    fun provideSendBugReportUseCase(useCase: SendBugReportImpl): SendBugReport

    @BindsOptionalOf
    fun optionalBugReportLogProvider(): BugReportLogProvider
}
