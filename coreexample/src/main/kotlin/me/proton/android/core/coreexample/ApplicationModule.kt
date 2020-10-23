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

package me.proton.android.core.coreexample

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.proton.android.core.coreexample.Constants.BASE_URL
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.ClientSecret
import me.proton.core.auth.domain.entity.Account
import me.proton.core.auth.domain.entity.KeySalts
import me.proton.core.auth.domain.entity.LoginInfo
import me.proton.core.auth.domain.entity.ScopeInfo
import me.proton.core.auth.domain.entity.SecondFactorProof
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.entity.User
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.arch.DataResult
import me.proton.core.humanverification.data.repository.HumanVerificationLocalRepositoryImpl
import me.proton.core.humanverification.data.repository.HumanVerificationRemoteRepositoryImpl
import me.proton.core.humanverification.domain.repository.HumanVerificationLocalRepository
import me.proton.core.humanverification.domain.repository.HumanVerificationRemoteRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.di.ApiFactory
import me.proton.core.network.data.di.NetworkManager
import me.proton.core.network.data.di.NetworkPrefs
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.Session
import me.proton.core.network.domain.session.SessionId
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.util.kotlin.DispatcherProvider
import javax.inject.Singleton

/**
 * Application module singleton for Hilt dependencies.
 */
@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager =
        NetworkManager(context)

    @Provides
    @Singleton
    fun provideNetworkPrefs(@ApplicationContext context: Context) =
        NetworkPrefs(context)

    @Provides
    @Singleton
    fun provideApiFactory(
        apiClient: ApiClient,
        networkManager: NetworkManager,
        networkPrefs: NetworkPrefs,
        sessionProvider: SessionProvider,
        sessionListener: SessionListener
    ): ApiFactory = ApiFactory(
        BASE_URL, apiClient, CoreExampleLogger(), networkManager, networkPrefs, sessionProvider, sessionListener,
        CoroutineScope(Job() + Dispatchers.Default)
    )

    @Provides
    fun provideSessionProvider(): SessionProvider = object : SessionProvider {
        override fun getSession(sessionId: SessionId): Session? {
            TODO("AccountManagerImpl implement SessionProvider.")
        }
    }

    @Provides
    fun provideSessionListener(): SessionListener = object : SessionListener {
        override fun onSessionTokenRefreshed(session: Session) {
            TODO("AccountManagerImpl implement SessionListener")
        }

        override fun onSessionForceLogout(session: Session) {
            TODO("AccountManagerImpl implement SessionListener")
        }

        override suspend fun onHumanVerificationNeeded(
            session: Session,
            details: HumanVerificationDetails?
        ): SessionListener.HumanVerificationResult {
            TODO("AccountManagerImpl implement SessionListener")
        }
    }

    @Provides
    fun provideAuthRepository(apiProvider: ApiProvider): AuthRepository = object : AuthRepository {
        /**
         * Get Login Info needed to start the login process.
         */
        override suspend fun getLoginInfo(username: String, clientSecret: String): DataResult<LoginInfo> {
            TODO("Not yet implemented")
        }

        /**
         * Perform Login to create a session (accessToken, refreshToken, sessionId, ...).
         */
        override suspend fun performLogin(
            username: String,
            clientSecret: String,
            clientEphemeral: String,
            clientProof: String,
            srpSession: String
        ): DataResult<SessionInfo> {
            TODO("Not yet implemented")
        }

        /**
         * Perform Two Factor for the Login process for a given [SessionId].
         */
        override suspend fun performSecondFactor(
            sessionId: SessionId,
            secondFactorProof: SecondFactorProof
        ): DataResult<ScopeInfo> {
            TODO("Not yet implemented")
        }

        /**
         * Returns the basic user information for a given [SessionId].
         */
        override suspend fun getUser(sessionId: SessionId): DataResult<User> {
            TODO("Not yet implemented")
        }

        /**
         * Returns the key salt information for a given [SessionId].
         */
        override suspend fun getSalts(sessionId: SessionId): DataResult<KeySalts> {
            TODO("Not yet implemented")
        }

        /**
         * Perform Two Factor for the Login process for a given [SessionId].
         */
        override suspend fun revokeSession(sessionId: SessionId): DataResult<Boolean> {
            TODO("Not yet implemented")
        }

    }

    @Provides
    @Singleton
    fun provideApiProvider(apiFactory: ApiFactory): ApiProvider =
        ApiProvider(apiFactory)

    @Provides
    fun provideLocalRepository(@ApplicationContext context: Context): HumanVerificationLocalRepository =
        HumanVerificationLocalRepositoryImpl(context)

    @Provides
    fun provideRemoteRepository(apiProvider: ApiProvider): HumanVerificationRemoteRepository =
        HumanVerificationRemoteRepositoryImpl(apiProvider)

    @Provides
    @ClientSecret
    fun provideClientSecret(): String = ""

    @Provides
    fun provideDispatcherProvider() = object : DispatcherProvider {
        override val Io = Dispatchers.IO
        override val Comp = Dispatchers.Default
        override val Main = Dispatchers.Main
    }

    @Provides
    @Singleton
    fun provideAccountWorkflowHandler(): AccountWorkflowHandler = object : AccountWorkflowHandler {
        /**
         * Handle a new [Session] for a new or existing [Account] from Login workflow.
         */
        override suspend fun handleSession(account: Account, session: Session) {
            TODO("Not yet implemented")
        }

        /**
         * Handle TwoPassMode success.
         */
        override suspend fun handleTwoPassModeSuccess(sessionId: SessionId) {
            TODO("Not yet implemented")
        }

        /**
         * Handle TwoPassMode failure.
         *
         * Note: The Workflow must succeed within maximum 10 min of authentication.
         */
        override suspend fun handleTwoPassModeFailed(sessionId: SessionId) {
            TODO("Not yet implemented")
        }

        /**
         * Handle SecondFactor success.
         *
         * @param updatedScopes the new updated full list of scopes.
         */
        override suspend fun handleSecondFactorSuccess(sessionId: SessionId, updatedScopes: List<String>) {
            TODO("Not yet implemented")
        }

        /**
         * Handle SecondFactor failure.
         *
         * Note: Maximum number of failure is 3, then the session will be invalidated and API will return HTTP 401.
         */
        override suspend fun handleSecondFactorFailed(sessionId: SessionId) {
            TODO("Not yet implemented")
        }

        /**
         * Handle HumanVerification success.
         *
         * Note: TokenType and tokenCode must be part of the next API calls.
         */
        override suspend fun handleHumanVerificationSuccess(
            sessionId: SessionId,
            tokenType: String,
            tokenCode: String
        ) {
            TODO("Not yet implemented")
        }

        /**
         * Handle HumanVerification failure.
         */
        override suspend fun handleHumanVerificationFailed(sessionId: SessionId) {
            TODO("Not yet implemented")
        }

    }
}
