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

package me.proton.core.userrecovery.presentation.compose

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.userrecovery.data.usecase.DeleteRecoveryFiles
import me.proton.core.userrecovery.data.usecase.ObserveUserDeviceRecovery
import me.proton.core.userrecovery.data.usecase.ObserveUsersWithInactiveKeysForRecovery
import me.proton.core.userrecovery.data.usecase.ObserveUsersWithRecoverySecretButNoFile
import me.proton.core.userrecovery.data.usecase.ObserveUsersWithoutRecoverySecret
import me.proton.core.userrecovery.data.usecase.StoreRecoveryFile
import me.proton.core.userrecovery.domain.LogTag
import me.proton.core.userrecovery.domain.usecase.GetRecoveryFile
import me.proton.core.userrecovery.domain.worker.UserRecoveryWorkerManager
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("LongParameterList")
class DeviceRecoveryHandler @Inject constructor(
    private val scopeProvider: CoroutineScopeProvider,
    private val deleteRecoveryFiles: DeleteRecoveryFiles,
    private val getRecoveryFile: GetRecoveryFile,
    private val observeUserDeviceRecovery: ObserveUserDeviceRecovery,
    private val observeUsersWithInactiveKeysForRecovery: ObserveUsersWithInactiveKeysForRecovery,
    private val observeUsersWithoutRecoverySecret: ObserveUsersWithoutRecoverySecret,
    private val observeUsersWithRecoverySecretButNoFile: ObserveUsersWithRecoverySecretButNoFile,
    private val storeRecoveryFile: StoreRecoveryFile,
    private val userRecoveryWorkerManager: UserRecoveryWorkerManager,
) {
    fun start() {
        // Upload a recovery secret if needed:
        observeUsersWithoutRecoverySecret()
            .onEach { userRecoveryWorkerManager.enqueueSetRecoverySecret(it) }
            .catch { CoreLogger.e(LogTag.DEFAULT, it) }
            .launchIn(scopeProvider.GlobalDefaultSupervisedScope)

        // Generate a recovery file if needed:
        observeUsersWithRecoverySecretButNoFile()
            .onEach {
                val result = getRecoveryFile(it)
                storeRecoveryFile(encodedRecoveryFile = result.recoveryFile, keyCount = result.keyCount, userId = it)
            }
            .catch { CoreLogger.e(LogTag.DEFAULT, it) }
            .launchIn(scopeProvider.GlobalDefaultSupervisedScope)

        // Recover private keys if possible:
        observeUsersWithInactiveKeysForRecovery()
            .onEach { userRecoveryWorkerManager.enqueueRecoverInactivePrivateKeys(it) }
            .catch { CoreLogger.e(LogTag.DEFAULT, it) }
            .launchIn(scopeProvider.GlobalDefaultSupervisedScope)

        // Remove local recovery files, if device recovery user setting is disabled:
        observeUserDeviceRecovery()
            .filter { (_, deviceRecovery) -> deviceRecovery == false }
            .onEach { (user, _) -> deleteRecoveryFiles(user.userId) }
            .catch { CoreLogger.e(LogTag.DEFAULT, it) }
            .launchIn(scopeProvider.GlobalDefaultSupervisedScope)
    }
}
