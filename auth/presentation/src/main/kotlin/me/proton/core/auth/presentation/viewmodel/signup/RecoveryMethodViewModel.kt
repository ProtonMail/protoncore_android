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

package me.proton.core.auth.presentation.viewmodel.signup

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.auth.domain.usecase.signup.ValidateEmail
import me.proton.core.auth.domain.usecase.signup.ValidatePhone
import me.proton.core.auth.presentation.entity.signup.RecoveryMethod
import me.proton.core.auth.presentation.entity.signup.RecoveryMethodType
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.util.kotlin.exhaustive
import javax.inject.Inject

@HiltViewModel
internal class RecoveryMethodViewModel @Inject constructor(
    private val validateEmail: ValidateEmail,
    private val validatePhone: ValidatePhone
) : ProtonViewModel() {

    private val _recoveryMethodUpdate = MutableStateFlow(RecoveryMethodType.EMAIL)
    private val _validationResult = MutableStateFlow<ViewModelResult<Boolean>>(ViewModelResult.None)

    val recoveryMethodUpdate = _recoveryMethodUpdate.asStateFlow()
    val validationResult = _validationResult.asStateFlow()

    private var _currentActiveRecoveryMethod: RecoveryMethod = RecoveryMethod(RecoveryMethodType.EMAIL, "")

    val recoveryMethod: RecoveryMethod
        get() = _currentActiveRecoveryMethod

    /** Called when destination (email or phone) is empty/blank. */
    fun onRecoveryMethodDestinationMissing() {
        _validationResult.tryEmit(ViewModelResult.Success(false))
    }

    lateinit var challengeHolder: ChallengeHolder

    /**
     * Sets the currently active verification method that the user chose.
     * If the user changes the verification method tab, the destination is being reset.
     */
    fun setActiveRecoveryMethod(
        clicks: Int = 0,
        focusTime: Long = 0,
        copies: List<String> = emptyList(),
        pastes: List<String> = emptyList(),
        keys: List<Char> = emptyList(),
        userSelectedMethodType: RecoveryMethodType,
        destination: String = "") {
        challengeHolder = ChallengeHolder(clicks, focusTime, copies, pastes, keys)
        _currentActiveRecoveryMethod = RecoveryMethod(userSelectedMethodType, destination)
        _recoveryMethodUpdate.tryEmit(userSelectedMethodType)
    }

    /**
     * Validates the user input recovery destination (email or phone number) on the API.
     */
    fun validateRecoveryDestinationInput() = flow {
        emit(ViewModelResult.Processing)
        emit(
            when (_currentActiveRecoveryMethod.type) {
                RecoveryMethodType.EMAIL -> validateRecoveryEmail()
                RecoveryMethodType.SMS -> validateRecoveryPhone()
            }.exhaustive
        )
    }.catch { error ->
        emit(ViewModelResult.Error(error))
    }.onEach {
        _validationResult.tryEmit(it)
    }.launchIn(viewModelScope)

    /**
     * Checks on the API if the email is a valid one.
     */
    private suspend fun validateRecoveryEmail() =
        ViewModelResult.Success(validateEmail(_currentActiveRecoveryMethod.destination))

    /**
     * Checks on the API if the phone is a valid one.
     */
    private suspend fun validateRecoveryPhone() =
        ViewModelResult.Success(validatePhone(_currentActiveRecoveryMethod.destination))

    data class ChallengeHolder(
        val clicks: Int = 0,
        val focusTime: Long = 0,
        val copies: List<String> = emptyList(),
        val pastes: List<String> = emptyList(),
        val keys: List<Char> = emptyList()
    )
}
