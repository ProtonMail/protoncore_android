/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.presentation.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import me.proton.core.accountmanager.domain.feature.IsCredentialLessEnabled
import me.proton.core.auth.presentation.BuildConfig
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
internal class AddAccountViewModel @Inject constructor(
    private val isCredentialLessEnabled: IsCredentialLessEnabled
) : ProtonViewModel() {
    suspend fun getNextScreen(): Screen {
        return if (isCredentialLessEnabled(userId = null)) { // TODO wait until feature flags are fetched
            Screen.CredentialLessFragment
        } else {
            Screen.AddAccountFragment
        }
    }

    enum class Screen {
        AddAccountFragment,
        CredentialLessFragment
    }
}
