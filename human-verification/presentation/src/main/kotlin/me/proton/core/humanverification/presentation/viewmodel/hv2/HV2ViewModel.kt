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

package me.proton.core.humanverification.presentation.viewmodel.hv2

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.entity.HumanVerificationToken
import me.proton.core.humanverification.presentation.ui.hv2.HV2DialogFragment
import me.proton.core.network.domain.client.ClientId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

/**
 * View model class to serve the main Human Verification screen.
 */
@HiltViewModel
class HV2ViewModel @Inject constructor(
    private val humanVerificationWorkflowHandler: HumanVerificationWorkflowHandler,
    savedStateHandle: SavedStateHandle
) : ProtonViewModel() {

    private lateinit var currentActiveVerificationMethod: TokenType

    private val availableVerificationMethods: List<String> =
        savedStateHandle.get<List<String>>(HV2DialogFragment.ARG_VERIFICATION_OPTIONS)!!

    private val _activeMethod = MutableStateFlow<String?>(null)
    private val _enabledMethods = MutableStateFlow<List<String>>(emptyList())

    val activeMethod = _activeMethod.asStateFlow()
    val enabledMethods = _enabledMethods.asStateFlow()

    init {
        _enabledMethods.tryEmit(availableVerificationMethods)
        // A list of all available methods that the API is currently supporting for this particular user and device.
        // The UI should present the verification methods for each one of them.
        // It is safe to use !! here, guaranteed that there will be at least 1 verification method available
        if (availableVerificationMethods.isNotEmpty()) {
            defineActiveVerificationMethod()
        }
    }

    /**
     * Sets the currently active verification method that the user chose.
     */
    fun defineActiveVerificationMethod(userSelectedMethod: TokenType? = null) {
        userSelectedMethod?.let {
            currentActiveVerificationMethod = it
        } ?: run {
            currentActiveVerificationMethod = TokenType.fromString(availableVerificationMethods[0])
        }
        _activeMethod.tryEmit(currentActiveVerificationMethod.value)
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
