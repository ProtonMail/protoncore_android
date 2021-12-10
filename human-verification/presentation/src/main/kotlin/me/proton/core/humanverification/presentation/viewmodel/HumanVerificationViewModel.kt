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

package me.proton.core.humanverification.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.presentation.entity.HumanVerificationToken
import me.proton.core.network.domain.client.ClientId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.usersettings.domain.usecase.GetSettings
import me.proton.core.util.kotlin.DispatcherProvider
import javax.inject.Inject

/**
 * View model class to serve the main Human Verification screen.
 */
@HiltViewModel
class HumanVerificationViewModel @Inject constructor(
    private val humanVerificationWorkflowHandler: HumanVerificationWorkflowHandler,
    private val accountRepository: AccountRepository,
    private val getSettings: GetSettings,
) : ProtonViewModel() {

    suspend fun getHumanVerificationExtraParams() = withContext(Dispatchers.IO) {
        val userId = accountRepository.getPrimaryUserId()
            .firstOrNull() ?: return@withContext null

        val settings = getSettings(userId)
        val defaultCountry = settings.locale?.substringAfter("_")
        HumanVerificationExtraParams(settings.phone?.value, settings.locale, defaultCountry)
    }

    fun onHumanVerificationResult(clientId: ClientId, token: HumanVerificationToken?): Job = viewModelScope.launch {
        if (token != null) {
            humanVerificationWorkflowHandler.handleHumanVerificationSuccess(
                clientId = clientId,
                tokenType = token.type,
                tokenCode = token.code
            )
        } else {
            humanVerificationWorkflowHandler.handleHumanVerificationFailed(clientId = clientId)
        }
    }
}

data class HumanVerificationExtraParams(
    val recoveryPhone: String?,
    val locale: String?,
    val defaultCountry: String?,
)
