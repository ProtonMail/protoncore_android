/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.test.android.di

import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import me.proton.android.core.coreexample.di.HumanVerificationModule
import me.proton.android.core.coreexample.di.WorkManagerModule
import me.proton.core.accountrecovery.dagger.CoreAccountRecoveryFeaturesModule
import me.proton.core.accountrecovery.domain.IsAccountRecoveryEnabled
import me.proton.core.accountrecovery.domain.IsAccountRecoveryResetEnabled
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.EnvironmentConfiguration
import me.proton.core.configuration.FeatureFlagsConfiguration
import me.proton.core.humanverification.presentation.HumanVerificationApiHost
import me.proton.core.humanverification.presentation.utils.HumanVerificationVersion
import me.proton.core.notification.dagger.CoreNotificationFeaturesModule
import me.proton.core.notification.domain.usecase.IsNotificationsEnabled
import me.proton.core.test.quark.Quark
import me.proton.core.test.quark.v2.QuarkCommand
import okhttp3.OkHttpClient
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        CoreAccountRecoveryFeaturesModule::class,
        CoreNotificationFeaturesModule::class,
        WorkManagerModule::class
    ]
)
object AndroidTestComponent {
    @Provides
    @Singleton
    fun provideIsAccountRecoveryEnabled(): IsAccountRecoveryEnabled = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideIsAccountRecoveryResetEnabled(): IsAccountRecoveryResetEnabled =
        mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideIsNotificationsEnabled(): IsNotificationsEnabled = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideWorkManager(hiltWorkerFactory: HiltWorkerFactory): WorkManager {
        val config = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .setExecutor(androidx.work.testing.SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            ApplicationProvider.getApplicationContext(),
            config
        )
        return WorkManager.getInstance(ApplicationProvider.getApplicationContext())
    }

    @Provides
    @Singleton
    fun provideQuarkCommand(envConfig: EnvironmentConfiguration): QuarkCommand {
        val timeout = 45.seconds.toJavaDuration()
        val quarkClient = OkHttpClient
            .Builder()
            .callTimeout(timeout)
            .readTimeout(timeout)
            .writeTimeout(timeout)
            .build()

        return QuarkCommand(quarkClient)
            .baseUrl("https://${envConfig.host}/api/internal")
            .proxyToken(envConfig.proxyToken)
    }

    @Provides
    @Singleton
    fun provideQuark(envConfig: EnvironmentConfiguration): Quark =
        Quark.fromDefaultResources(envConfig.host, envConfig.proxyToken)

    @Provides
    @Singleton
    fun provideFeatureFlagConfig(
        contentResolverConfigManager: ContentResolverConfigManager
    ): FeatureFlagsConfiguration {
        val configData =
            contentResolverConfigManager.queryAtClassPath(EnvironmentConfiguration::class)
        return FeatureFlagsConfiguration.fromMap(configData ?: emptyMap())
    }
}