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

package me.proton.core.auth.presentation.compose.sso.backuppassword.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.auth.presentation.compose.sso.backuppassword.setup.BackupPasswordSetupScreen.getUserId
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.presentation.utils.InputValidationResult
import me.proton.core.presentation.utils.ValidationType
import me.proton.core.presentation.utils.onFailure
import me.proton.core.presentation.utils.then
import me.proton.core.usersettings.domain.usecase.GetOrganization
import javax.inject.Inject

@HiltViewModel
public class BackupPasswordSetupViewModel @Inject constructor(
    private val getOrganization: GetOrganization,
    private val product: Product,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val userId: UserId by lazy { savedStateHandle.getUserId() }

    private val _state: MutableStateFlow<BackupPasswordSetupUiState> = MutableStateFlow(BackupPasswordSetupUiState.Idle)
    public val state: StateFlow<BackupPasswordSetupUiState> = _state.asStateFlow()

    /** UI data that always displayed, no matter the [state]. */
    public val data: StateFlow<BackupPasswordSetupUiData> = loadData(userId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis),
        BackupPasswordSetupUiData(product = product)
    )

    public fun submit(action: BackupPasswordSetupAction): Job = viewModelScope.launch {
        when (action) {
            is BackupPasswordSetupAction.Submit -> onSubmit(action)
        }
    }

    private suspend fun onSubmit(action: BackupPasswordSetupAction.Submit) = try {
        _state.emit(BackupPasswordSetupUiState.Loading)
        validateForm(action.backupPassword, action.repeatBackupPassword)
        setupBackupPassword(action.backupPassword)
        _state.emit(BackupPasswordSetupUiState.Success)
    } catch (e: ApiException) {
        _state.emit(BackupPasswordSetupUiState.Error(e))
    } catch (e: FormValidationError) {
        _state.emit(BackupPasswordSetupUiState.FormError(e.formError))
    }

    private fun loadData(userId: UserId) = flow {
        _state.emit(BackupPasswordSetupUiState.Loading)

        val organization = requireNotNull(getOrganization(userId, false)) {
            "Organization not found for user ${userId}."
        }

        emit(
            BackupPasswordSetupUiData(
                product = product,
                organizationAdminEmail = null, // TODO get from BE
                organizationIcon = null, // TODO get from BE /api/core/{_version}/organizations/logo/{enc_id}
                organizationName = organization.displayName
            )
        )
        _state.emit(BackupPasswordSetupUiState.Idle)
    }.catch {
        when (it) {
            is ApiException -> _state.emit(BackupPasswordSetupUiState.Error(it))
            else -> throw it
        }
    }

    private fun setupBackupPassword(backupPassword: String) {
        // TODO setup the backupPassword with the BE
        //  Note: should throw an error if the setup fails.
    }

    /**
     * @throws FormValidationError in case of invalid form.
     */
    private fun validateForm(backupPassword: String, repeatBackupPassword: String) {
        InputValidationResult(
            text = backupPassword,
            validationType = ValidationType.PasswordMinLength,
        ).onFailure {
            throw FormValidationError(BackupPasswordSetupFormError.PasswordTooShort)
        }.then(
            InputValidationResult(
                text = backupPassword,
                validationType = ValidationType.PasswordMatch,
                additionalText = repeatBackupPassword
            )
        )?.onFailure {
            throw FormValidationError(BackupPasswordSetupFormError.PasswordsDoNotMatch)
        }
    }

    private class FormValidationError(val formError: BackupPasswordSetupFormError) : Throwable()
}
