/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import me.proton.core.auth.domain.entity.SessionForkSelector
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.auth.domain.usecase.fork.DecryptPassphrasePayload
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.devicemigration.domain.LogTag
import me.proton.core.devicemigration.domain.entity.EncryptionKey
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.ApiResult
import me.proton.core.network.domain.HttpResponseCodes.HTTP_UNPROCESSABLE
import me.proton.core.network.domain.isHttpError
import me.proton.core.network.domain.session.Session
import me.proton.core.util.kotlin.catchAll
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public class PullEdmSessionFork @Inject constructor(
    private val authRepository: AuthRepository,
    private val decryptPassphrasePayload: DecryptPassphrasePayload,
) {
    public operator fun invoke(
        encryptionKey: EncryptionKey,
        selector: SessionForkSelector,
        pollDuration: Duration = 10.seconds
    ): Flow<Result> = flow {
        emit(Result.Loading)
        val (payload, session) = authRepository.getForkedSession(selector)
        val passphrase = decryptPassphrasePayload(
            payload = payload,
            encryptionKey = encryptionKey.value,
            aesCipherGCMTagBits = EDM_AES_CIPHER_GCM_TAG_BITS,
            aesCipherIvBytes = EDM_AES_CIPHER_IV_BYTES
        )
        emit(Result.Success(passphrase, session))
    }.retryWhen { cause: Throwable, _: Long ->
        when (cause) {
            is ApiException -> {
                when {
                    cause.isHttpError(HTTP_UNPROCESSABLE) || cause.error is ApiResult.Error.Connection -> {
                        emit(Result.Awaiting)
                        delay(pollDuration)
                        true
                    }

                    else -> false
                }
            }

            else -> false
        }
    }.catchAll(LogTag.TARGET_PULLING_FORK) {
        emit(Result.UnrecoverableError(it))
    }

    public sealed interface Result {
        public data object Awaiting : Result
        public data object Loading : Result
        public data class Success(
            val passphrase: EncryptedByteArray?,
            val session: Session.Authenticated
        ) : Result

        public data class UnrecoverableError(val cause: Throwable) : Result
    }
}
