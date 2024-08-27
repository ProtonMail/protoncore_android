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

package me.proton.core.auth.presentation.compose.confirmationcode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.usecase.sso.GenerateConfirmationCode
import me.proton.core.auth.presentation.compose.confirmationcode.ShareConfirmationCodeWithAdminScreen.getUserId
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.ApiException
import me.proton.core.user.domain.usecase.GetUser
import javax.inject.Inject

@HiltViewModel
public class ShareConfirmationCodeWithAdminViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getUser: GetUser,
    private val generateConfirmationCode: GenerateConfirmationCode
) : ViewModel() {

    private val userId by lazy { savedStateHandle.getUserId() }

    private val actions = MutableSharedFlow<ShareConfirmationCodeAction>()

    public val state: StateFlow<ShareConfirmationCodeWithAdminState> = load()
        .flatMapLatest {
            actions
                .transform { action ->
                    val flow = when (action) {
                        ShareConfirmationCodeAction.Cancel,
                        ShareConfirmationCodeAction.Close -> onClose()

                        ShareConfirmationCodeAction.UseBackUpPassword -> useBackUpPassword()
                    }
                    emitAll(flow)
                }.catch {
                    emit(ShareConfirmationCodeWithAdminState.Error(it.message))
                }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis),
            ShareConfirmationCodeWithAdminState.Loading
        )

    public fun submit(action: ShareConfirmationCodeAction): Job = viewModelScope.launch {
        actions.emit(action)
    }

    private fun load() = flow {
        emit(ShareConfirmationCodeWithAdminState.Loading)
        val user = getUser(userId, false)
        emit(
            ShareConfirmationCodeWithAdminState.DataLoaded(
                username = user.email ?: user.name ?: "",
                confirmationCode = generateConfirmationCode(userId)
            )
        )
    }.catch {
        when (it) {
            is ApiException -> emit(ShareConfirmationCodeWithAdminState.Error(it.message))
            else -> throw it
        }
    }

    private fun useBackUpPassword() = flow<ShareConfirmationCodeWithAdminState> {
        // TODO:
    }

    private fun onClose() = flow<ShareConfirmationCodeWithAdminState> {
        emit(ShareConfirmationCodeWithAdminState.Close)
    }
}

public object ShareConfirmationCodeWithAdminScreen {
    public const val KEY_USERID: String = "UserId"
    public fun SavedStateHandle.getUserId(): UserId = UserId(get<String>(KEY_USERID)!!)
}
