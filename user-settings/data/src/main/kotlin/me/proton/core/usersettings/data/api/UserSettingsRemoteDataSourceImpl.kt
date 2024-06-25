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

package me.proton.core.usersettings.data.api

import me.proton.core.auth.data.api.request.toFido2Request
import me.proton.core.auth.domain.entity.ServerProof
import me.proton.core.auth.fido.domain.entity.SecondFactorFido
import me.proton.core.crypto.common.pgp.Based64Encoded
import me.proton.core.crypto.common.pgp.EncryptedSignature
import me.proton.core.crypto.common.srp.Auth
import me.proton.core.crypto.common.srp.SrpProofs
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.key.data.api.request.AuthRequest
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.data.protonApi.isSuccess
import me.proton.core.user.domain.extension.isCredentialLess
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.data.api.request.SetRecoverySecretRequest
import me.proton.core.usersettings.data.api.request.SetUsernameRequest
import me.proton.core.usersettings.data.api.request.UpdateCrashReportsRequest
import me.proton.core.usersettings.data.api.request.UpdateLoginPasswordRequest
import me.proton.core.usersettings.data.api.request.UpdateRecoveryEmailRequest
import me.proton.core.usersettings.data.api.request.UpdateTelemetryRequest
import me.proton.core.usersettings.data.api.response.SingleUserSettingsResponse
import me.proton.core.usersettings.data.extension.fromResponse
import me.proton.core.usersettings.data.extension.toUserSettings
import me.proton.core.usersettings.domain.entity.UserSettings
import me.proton.core.usersettings.domain.entity.UserSettingsProperty
import me.proton.core.usersettings.domain.repository.UserSettingsRemoteDataSource
import me.proton.core.util.kotlin.coroutine.result
import me.proton.core.util.kotlin.exhaustive
import me.proton.core.util.kotlin.toInt
import javax.inject.Inject

class UserSettingsRemoteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val userRepository: UserRepository
) : UserSettingsRemoteDataSource {

    override suspend fun fetch(userId: UserId): UserSettings =
        when (userRepository.getUser(userId).isCredentialLess()) {
            true -> makeUserSettingsForCredentialLess(userId)
            false -> apiProvider.get<UserSettingsApi>(userId).invoke {
                getUserSettings().settings.fromResponse(userId)
            }.valueOrThrow
        }

    override suspend fun setUsername(
        userId: UserId,
        username: String
    ): Boolean = apiProvider.get<UserSettingsApi>(userId).invoke {
        setUsername(SetUsernameRequest(username = username)).isSuccess()
    }.valueOrThrow

    override suspend fun setRecoverySecret(
        userId: UserId,
        secret: Based64Encoded,
        signature: EncryptedSignature
    ): Boolean = apiProvider.get<UserSettingsApi>(userId).invoke {
        setRecoverySecret(SetRecoverySecretRequest(secret = secret, signature = signature)).isSuccess()
    }.valueOrThrow

    override suspend fun updateRecoveryEmail(
        sessionUserId: SessionUserId,
        email: String,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorCode: String?,
        secondFactorFido: SecondFactorFido?
    ): Pair<UserSettings, ServerProof> =
        result("updateRecoveryEmail") {
            apiProvider.get<UserSettingsApi>(sessionUserId).invoke {
                updateRecoveryEmail(
                    UpdateRecoveryEmailRequest(
                        email = email,
                        twoFactorCode = secondFactorCode,
                        fido2 = secondFactorFido?.toFido2Request(),
                        clientEphemeral = srpProofs.clientEphemeral,
                        clientProof = srpProofs.clientProof,
                        srpSession = srpSession
                    )
                ).let { response ->
                    response.settings.toUserSettings(sessionUserId) to response.serverProof
                }
            }.valueOrThrow
        }

    override suspend fun updateLoginPassword(
        sessionUserId: SessionUserId,
        srpProofs: SrpProofs,
        srpSession: String,
        secondFactorCode: String?,
        secondFactorFido: SecondFactorFido?,
        auth: Auth
    ): Pair<UserSettings, ServerProof> =
        result("updateLoginPassword") {
            apiProvider.get<UserSettingsApi>(sessionUserId).invoke {
                updateLoginPassword(
                    UpdateLoginPasswordRequest(
                        twoFactorCode = secondFactorCode,
                        fido2 = secondFactorFido?.toFido2Request(),
                        clientEphemeral = srpProofs.clientEphemeral,
                        clientProof = srpProofs.clientProof,
                        srpSession = srpSession,
                        auth = AuthRequest.from(auth)
                    )
                ).let { response ->
                    response.settings.toUserSettings(sessionUserId) to response.serverProof
                }
            }.valueOrThrow
        }

    override suspend fun updateUserSettings(
        userId: UserId,
        property: UserSettingsProperty
    ): UserSettings = apiProvider.get<UserSettingsApi>(userId).invoke {
        updateRemoteProperty(property).settings.toUserSettings(userId)
    }.valueOrThrow

    private suspend fun UserSettingsApi.updateRemoteProperty(
        property: UserSettingsProperty
    ): SingleUserSettingsResponse = when (property) {
        is UserSettingsProperty.CrashReports -> updateCrashReports(UpdateCrashReportsRequest(property.value.toInt()))
        is UserSettingsProperty.Telemetry -> updateTelemetry(UpdateTelemetryRequest(property.value.toInt()))
    }.exhaustive

    private fun makeUserSettingsForCredentialLess(userId: UserId) = UserSettings.nil(userId)
}
