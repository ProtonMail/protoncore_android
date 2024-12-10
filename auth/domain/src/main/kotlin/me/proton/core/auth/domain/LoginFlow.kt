/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.auth.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.LoginState.Error
import me.proton.core.auth.domain.LoginState.LoggedIn
import me.proton.core.auth.domain.entity.AuthInfo
import me.proton.core.auth.domain.entity.SessionInfo
import me.proton.core.auth.domain.feature.IsSsoEnabled
import me.proton.core.auth.domain.usecase.CreateLoginSession
import me.proton.core.auth.domain.usecase.CreateLoginSsoSession
import me.proton.core.auth.domain.usecase.GetAuthInfoAuto
import me.proton.core.auth.domain.usecase.GetAuthInfoSrp
import me.proton.core.auth.domain.usecase.GetAuthInfoSso
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup.Result.AccountReady
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup.Result.Error.UnlockPrimaryKeyError
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup.Result.Error.UserCheckError
import me.proton.core.auth.domain.usecase.PostLoginAccountSetup.Result.Need
import me.proton.core.auth.domain.usecase.PostLoginSsoAccountSetup
import me.proton.core.auth.domain.usecase.primaryKeyExists
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.network.domain.isExternalNotSupported
import me.proton.core.network.domain.isPotentialBlocking
import me.proton.core.network.domain.isSwitchToSrp
import me.proton.core.network.domain.isSwitchToSso
import me.proton.core.network.domain.isWrongPassword
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.catchAll
import me.proton.core.util.kotlin.catchWhen
import me.proton.core.util.kotlin.retryOnceWhen
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AuthSecret {
    /** For regular Proton account (with username/password).*/
    data class Srp(val password: String) : AuthSecret

    /** For external account (e.g. External/Global SSO).*/
    data class Sso(val token: String) : AuthSecret
}

@Singleton
class LoginFlow @Inject constructor(
    private val requiredAccountType: AccountType,
    private val keyStoreCrypto: KeyStoreCrypto,
    private val accountWorkflow: AccountWorkflowHandler,
    private val isSsoEnabled: IsSsoEnabled,
    private val getAuthInfoAuto: GetAuthInfoAuto,
    private val getAuthInfoSrp: GetAuthInfoSrp,
    private val getAuthInfoSso: GetAuthInfoSso,
    private val createLoginSession: CreateLoginSession,
    private val createLoginSsoSession: CreateLoginSsoSession,
    private val postLoginAccountSetup: PostLoginAccountSetup,
    private val postLoginSsoAccountSetup: PostLoginSsoAccountSetup,
) {
    private val authInfoCache = mutableMapOf<String, AuthInfo.Srp>()

    private fun AuthInfo.putInfo() = when (this) {
        is AuthInfo.Srp -> authInfoCache[username] = this
        is AuthInfo.Sso -> Unit
    }.let { this }

    private fun removeInfo(username: String) = authInfoCache.remove(username)

    operator fun invoke(
        username: String,
    ): Flow<LoginState> = onGetAuthInfo(username)

    operator fun invoke(
        username: String,
        secret: AuthSecret,
    ): Flow<LoginState> = when (secret) {
        is AuthSecret.Srp -> onCreateSrpSession(username, secret.password)
        is AuthSecret.Sso -> onCreateSsoSession(username, secret.token)
    }

    private fun onGetAuthInfo(username: String): Flow<LoginState> = flow {
        emit(LoginState.Processing)
        val info = if (isSsoEnabled()) {
            getAuthInfoAuto.invoke(sessionId = null, username = username).putInfo()
        } else {
            getAuthInfoSrp.invoke(sessionId = null, username = username).putInfo()
        }
        emit(LoginState.NeedAuthSecret(info))
    }.catchWhen(Throwable::isSwitchToSrp) {
        val info = getAuthInfoSrp.invoke(sessionId = null, username = username)
        emit(LoginState.NeedAuthSecret(info))
    }.catchWhen(Throwable::isSwitchToSso) {
        if (isSsoEnabled()) {
            val info = getAuthInfoSso.invoke(sessionId = null, email = username)
            emit(LoginState.NeedAuthSecret(info))
        } else {
            emit(Error.ExternalSsoNotSupported)
        }
    }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
        emit(Error.Message(it, it.isPotentialBlocking()))
    }

    private fun onCreateSrpSession(
        username: String,
        password: String,
    ): Flow<LoginState> = flow {
        emit(LoginState.Processing)
        val info = removeInfo(username)
        val encryptedPassword = password.encrypt(keyStoreCrypto)
        val sessionInfo = createLoginSession(username, encryptedPassword, requiredAccountType, info)
        emitAll(onSrpSessionCreated(sessionInfo, encryptedPassword))
    }.catchWhen(Throwable::isWrongPassword) {
        emit(Error.InvalidPassword(it))
    }.catchWhen(Throwable::isExternalNotSupported) {
        emit(Error.ExternalEmailNotSupported(it))
    }.catchWhen(Throwable::isSwitchToSso) {
        emit(Error.SwitchToSso(username, it))
    }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
        emit(Error.Message(it, it.isPotentialBlocking()))
    }

    private fun onCreateSsoSession(
        username: String,
        token: String,
    ): Flow<LoginState> = flow {
        emit(LoginState.Processing)
        val sessionInfo = createLoginSsoSession(username, token, requiredAccountType)
        emitAll(onSsoSessionCreated(sessionInfo))
    }.catchWhen(Throwable::isWrongPassword) {
        emit(Error.InvalidPassword(it))
    }.catchWhen(Throwable::isExternalNotSupported) {
        emit(Error.ExternalEmailNotSupported(it))
    }.catchWhen(Throwable::isSwitchToSrp) {
        emit(Error.SwitchToSrp(username, it))
    }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
        emit(Error.Message(it, it.isPotentialBlocking()))
    }

    private fun onSrpSessionCreated(
        sessionInfo: SessionInfo,
        encryptedPassword: EncryptedString,
    ): Flow<LoginState> = flow {
        emit(LoginState.Processing)
        val result = postLoginAccountSetup(
            userId = sessionInfo.userId,
            encryptedPassword = encryptedPassword,
            requiredAccountType = requiredAccountType,
            isSecondFactorNeeded = sessionInfo.isSecondFactorNeeded,
            isTwoPassModeNeeded = sessionInfo.isTwoPassModeNeeded,
            temporaryPassword = sessionInfo.temporaryPassword
        )
        emitAll(onAccountSetupResult(sessionInfo, result))
    }.retryOnceWhen(Throwable::primaryKeyExists) {
        CoreLogger.e(LogTag.FLOW_ERROR_RETRY, it, "Retrying login flow")
    }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
        accountWorkflow.handleAccountDisabled(sessionInfo.userId)
        emit(Error.Message(it, it.isPotentialBlocking()))
    }

    private fun onSsoSessionCreated(
        sessionInfo: SessionInfo,
    ): Flow<LoginState> = flow {
        emit(LoginState.Processing)
        val result = postLoginSsoAccountSetup(sessionInfo.userId)
        emitAll(onAccountSetupResult(sessionInfo, result))
    }.catchAll(LogTag.FLOW_ERROR_LOGIN) {
        accountWorkflow.handleAccountDisabled(sessionInfo.userId)
        emit(Error.Message(it, it.isPotentialBlocking()))
    }

    private fun onAccountSetupResult(
        sessionInfo: SessionInfo,
        result: PostLoginAccountSetup.Result
    ) = flow {
        val state = when (result) {
            is UnlockPrimaryKeyError -> Error.UnlockPrimaryKey
            is UserCheckError -> Error.UserCheck(result.error.localizedMessage, result.error.action)
            is Need.ChangePassword -> Error.ChangePassword
            is Need.ChooseUsername -> LoggedIn(sessionInfo.userId)
            is Need.DeviceSecret -> LoggedIn(sessionInfo.userId)
            is Need.SecondFactor -> LoggedIn(sessionInfo.userId)
            is Need.TwoPassMode -> LoggedIn(sessionInfo.userId)
            is AccountReady -> LoggedIn(sessionInfo.userId)
        }
        emit(state)
    }
}
