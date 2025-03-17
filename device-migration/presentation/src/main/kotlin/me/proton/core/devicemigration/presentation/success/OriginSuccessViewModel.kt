/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.presentation.success

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import me.proton.core.compose.viewmodel.BaseViewModel
import me.proton.core.devicemigration.presentation.DeviceMigrationRoutes.Arg.getUserId
import me.proton.core.devicemigration.presentation.R
import me.proton.core.domain.entity.UserId
import me.proton.core.network.presentation.util.getUserMessage
import me.proton.core.user.domain.UserManager
import javax.inject.Inject

@HiltViewModel
internal class OriginSuccessViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val userManager: UserManager
) : BaseViewModel<OriginSuccessAction, OriginSuccessState>(OriginSuccessAction.Load, OriginSuccessState.Loading) {
    private val userId: UserId by lazy { savedStateHandle.getUserId() }

    override fun onAction(action: OriginSuccessAction): Flow<OriginSuccessState> = when (action) {
        is OriginSuccessAction.Load -> onLoad()
    }

    override suspend fun FlowCollector<OriginSuccessState>.onError(throwable: Throwable) {
        emit(
            OriginSuccessState.Error.Unknown(
                throwable.getUserMessage(context.resources) ?: context.getString(R.string.presentation_error_general)
            )
        )
    }

    private fun onLoad() = flow {
        emit(OriginSuccessState.Loading)
        val user = userManager.getUser(userId)
        emit(OriginSuccessState.Idle(user.email ?: user.name ?: ""))
    }
}
