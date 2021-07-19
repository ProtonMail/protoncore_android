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

package me.proton.android.core.coreexample.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.android.core.coreexample.PLAN_PLUS_ID
import me.proton.android.core.coreexample.PLAN_VISIONARY_ID
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.PaymentsOrchestrator
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.onPaymentResult
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.plan.presentation.onUpgradeResult
import me.proton.core.presentation.utils.showToast
import me.proton.core.settings.presentation.SettingsOrchestrator
import javax.inject.Inject

@HiltViewModel
class UserSettingsViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val settingsOrchestrator: SettingsOrchestrator
) : ViewModel() {

    fun register(context: ComponentActivity) {
        settingsOrchestrator.register(context)
    }

    private fun getPrimaryUserId() = accountManager.getPrimaryUserId()

    fun onUpdateRecoveryEmailClicked(context: ComponentActivity) {
        viewModelScope.launch {
            getPrimaryUserId().first()?.let {
                val account = accountManager.getAccount(it).first() ?: return@launch
                settingsOrchestrator.startUpdateRecoveryEmailWorkflow()
            }
        }
    }
}
