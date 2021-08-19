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

package me.proton.android.core.coreexample.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.transformLatest
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.mailsettings.domain.entity.MailSettings
import me.proton.core.mailsettings.domain.repository.MailSettingsRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MailSettingsViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val mailSettingsRepository: MailSettingsRepository
) : ViewModel() {

    sealed class MailSettingsState {
        data class Success(val mailSettings: MailSettings) : MailSettingsState()
        sealed class Error : MailSettingsState() {
            data class Message(val message: String?) : Error()
        }
    }

    fun getMailSettingsState() = accountManager.getPrimaryAccount()
        .filterIsInstance<Account>()
        .transformLatest { account ->
            emit(MailSettingsState.Success(mailSettingsRepository.getMailSettings(account.userId)) as MailSettingsState)
        }.catch { error ->
            Timber.e(error)
            emit(MailSettingsState.Error.Message(error.message))
        }
}
