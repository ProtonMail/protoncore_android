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

package me.proton.core.plan.presentation.compose.viewmodel

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.compose.viewmodel.stopTimeoutMillis
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.plan.presentation.compose.usecase.ShouldUpgradeStorage
import me.proton.core.plan.presentation.compose.usecase.ShouldUpgradeStorage.Result.DriveStorageUpgrade
import me.proton.core.plan.presentation.compose.usecase.ShouldUpgradeStorage.Result.MailStorageUpgrade
import me.proton.core.plan.presentation.compose.usecase.ShouldUpgradeStorage.Result.NoUpgrade
import me.proton.core.plan.presentation.compose.viewmodel.AccountStorageState.Hidden
import me.proton.core.plan.presentation.compose.viewmodel.AccountStorageState.HighStorageUsage.Drive
import me.proton.core.plan.presentation.compose.viewmodel.AccountStorageState.HighStorageUsage.Mail
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
public class UpgradeStorageInfoViewModel @Inject constructor(
    private val plansOrchestrator: PlansOrchestrator,
    shouldUpgradeStorage: ShouldUpgradeStorage,
) : ProtonViewModel() {
    public val state: StateFlow<AccountStorageState> = shouldUpgradeStorage()
        .map { it.toAccountStorageState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), INITIAL_STATE)

    internal fun perform(action: Action) = when (action) {
        is Action.Upgrade -> onUpgrade(action)
    }

    private fun onUpgrade(action: Action.Upgrade) {
        plansOrchestrator.startUpgradeWorkflow(action.userId)
    }

    internal companion object {
        val INITIAL_STATE = Hidden
    }

    internal sealed class Action {
        data class Upgrade(val userId: UserId) : Action()
    }
}

public sealed class AccountStorageState {
    public object Hidden : AccountStorageState()
    public sealed class HighStorageUsage(
        public open val percentage: Int,
        public open val userId: UserId
    ) : AccountStorageState() {
        public data class Drive(
            override val percentage: Int,
            override val userId: UserId
        ) : HighStorageUsage(percentage, userId)

        public data class Mail(
            override val percentage: Int,
            override val userId: UserId
        ) : HighStorageUsage(percentage, userId)
    }
}

private fun ShouldUpgradeStorage.Result.toAccountStorageState(): AccountStorageState = when (this) {
    is DriveStorageUpgrade -> Drive(storagePercentage, userId)
    is MailStorageUpgrade -> Mail(storagePercentage, userId)
    is NoUpgrade -> Hidden
}
