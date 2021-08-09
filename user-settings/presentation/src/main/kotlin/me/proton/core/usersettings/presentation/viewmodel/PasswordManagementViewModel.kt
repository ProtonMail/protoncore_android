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

package me.proton.core.usersettings.presentation.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.usersettings.domain.usecase.GetSettings
import me.proton.core.usersettings.domain.usecase.PerformUpdateRecoveryEmail
import javax.inject.Inject

@HiltViewModel
class PasswordManagementViewModel @Inject constructor(
    private val keyStoreCrypto: KeyStoreCrypto,
    private val getSettings: GetSettings,
    private val userRepository: UserRepository,
    private val performUpdateRecoveryEmail: PerformUpdateRecoveryEmail
) : ProtonViewModel() {

    fun updateLoginPassword() {

    }

    fun updateMailboxPassword() {

    }
}