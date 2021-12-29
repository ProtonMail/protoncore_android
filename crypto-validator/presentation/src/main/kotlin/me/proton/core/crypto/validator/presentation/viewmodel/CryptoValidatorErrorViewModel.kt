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

package me.proton.core.crypto.validator.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.validator.domain.prefs.CryptoPrefs
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
internal class CryptoValidatorErrorViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val cryptoPrefs: CryptoPrefs,
) : ProtonViewModel() {

    val shouldShowDialog: Boolean get() = cryptoPrefs.useInsecureKeystore != true

    val hasAccounts = accountManager.getAccounts()
        .map { accounts -> accounts.isNotEmpty() }
        .shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    fun allowInsecureKeystore() {
        cryptoPrefs.useInsecureKeystore = true
    }

    suspend fun removeAllAccounts() {
        accountManager.getAccounts().first().forEach {
            accountManager.removeAccount(it.userId)
        }
    }

}
