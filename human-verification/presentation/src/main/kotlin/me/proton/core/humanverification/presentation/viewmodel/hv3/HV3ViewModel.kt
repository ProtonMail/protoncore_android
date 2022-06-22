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

package me.proton.core.humanverification.presentation.viewmodel.hv3

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.domain.entity.Product
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.presentation.entity.HumanVerificationToken
import me.proton.core.network.domain.NetworkPrefs
import me.proton.core.network.domain.client.ClientId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.usersettings.domain.usecase.GetUserSettings
import javax.inject.Inject

/**
 * View model class to serve the main Human Verification screen.
 */
@HiltViewModel
class HV3ViewModel @Inject constructor(
    private val humanVerificationWorkflowHandler: HumanVerificationWorkflowHandler,
    private val accountRepository: AccountRepository,
    private val getUserSettings: GetUserSettings,
    private val networkPrefs: NetworkPrefs,
    private val product: Product,
) : ProtonViewModel() {

    private val backgroundContext = Dispatchers.IO + viewModelScope.coroutineContext

    val activeAltUrlForDoH: String? get() = networkPrefs.activeAltBaseUrl?.let {
        if (!it.endsWith("/")) "$it/" else it
    }

    suspend fun getHumanVerificationExtraParams() = withContext(backgroundContext) {
        val userId = accountRepository.getPrimaryUserId()
            .firstOrNull()

        val settings = userId?.let { getUserSettings(it, refresh = false) }
        val defaultCountry = settings?.locale?.substringAfter("_")
        HV3ExtraParams(
            settings?.phone?.value,
            settings?.locale,
            defaultCountry,
            product == Product.Vpn
        )
    }

    suspend fun onHumanVerificationResult(
        clientId: ClientId,
        token: HumanVerificationToken?
    ) = withContext(backgroundContext) {
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

