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

package me.proton.core.configuration.configurator.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.proton.core.configuration.ContentResolverConfigManager
import me.proton.core.configuration.configurator.domain.ConfigurationUseCase
import me.proton.core.configuration.configurator.domain.EnvironmentConfigurationUseCase
import me.proton.core.configuration.configurator.domain.FeatureFlagsConfigurationUseCase
import me.proton.core.configuration.configurator.domain.FeatureFlagsUseCase
import me.proton.core.configuration.configurator.entity.AppConfig
import me.proton.core.configuration.configurator.presentation.viewModel.SharedData
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.util.kotlin.CoroutineScopeProvider
import okhttp3.OkHttpClient
import javax.inject.Singleton
import kotlin.time.toJavaDuration

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {
    @Singleton
    @Provides
    fun provideQuarkCommand(client: OkHttpClient): QuarkCommand = QuarkCommand(client)

    @Provides
    @Singleton
    fun provideSharedData(): SharedData {
        return SharedData
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(appConfig: AppConfig): OkHttpClient {
        val clientTimeout = appConfig.quarkTimeout.toJavaDuration()
        return OkHttpClient.Builder().connectTimeout(clientTimeout)
            .readTimeout(clientTimeout)
            .writeTimeout(clientTimeout)
            .callTimeout(clientTimeout)
            .retryOnConnectionFailure(false)
            .build()
    }

    @Singleton
    @Provides
    fun provideContentResolverConfigurationUseCase(
        contentResolverConfigManager: ContentResolverConfigManager,
        appConfig: AppConfig,
        quark: QuarkCommand
    ): ConfigurationUseCase =
        EnvironmentConfigurationUseCase(quark, contentResolverConfigManager, appConfig)

    @Singleton
    @Provides
    fun provideContentResolverFeatureFlagUseCase(
        contentResolverConfigManager: ContentResolverConfigManager,
        featureFlagsDataStore: DataStore<Preferences>
    ): FeatureFlagsUseCase =
        FeatureFlagsConfigurationUseCase(contentResolverConfigManager, featureFlagsDataStore)


    private val Context.featureFlagsDataStore: DataStore<Preferences> by preferencesDataStore(name = "feature_flags_datastore")

    @Singleton
    @Provides
    fun provideFeatureFlagsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.featureFlagsDataStore

    @Provides
    @Singleton
    fun provideCoroutineScopeProvider(): CoroutineScopeProvider =
        object : CoroutineScopeProvider {
            override val GlobalDefaultSupervisedScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            override val GlobalIOSupervisedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        }

    @Provides
    @Singleton
    fun provideSessionProvider(): SessionProvider =
        object : SessionProvider {
            override suspend fun getSession(sessionId: SessionId?): Session? = null
            override suspend fun getSessions(): List<Session> = emptyList()
            override suspend fun getSessionId(userId: UserId?): SessionId? = null
            override suspend fun getUserId(sessionId: SessionId): UserId? = null
        }

    @Provides
    @Singleton
    fun provideSessionListener(): SessionListener =
        object : SessionListener {
            override suspend fun <T> withLock(sessionId: SessionId?, action: suspend () -> T): T = action()
            override suspend fun requestSession(): Boolean = false
            override suspend fun refreshSession(session: Session): Boolean = false
            override suspend fun onSessionTokenCreated(userId: UserId?, session: Session) {}
            override suspend fun onSessionTokenRefreshed(session: Session) {}
            override suspend fun onSessionScopesRefreshed(sessionId: SessionId, scopes: List<String>) {}
            override suspend fun onSessionForceLogout(session: Session, httpCode: Int) {}
        }
}
