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

package me.proton.core.auth.presentation

import android.content.Context
import android.content.Intent
import me.proton.core.auth.presentation.alert.confirmpass.StartConfirmPassword
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordInput
import me.proton.core.network.domain.scopes.MissingScopeState

/**
 * Starts the Confirm Password workflow.
 */
fun MissingScopeState.ScopeMissing.startConfirmPasswordWorkflow(context: Context) {
    val intent = StartConfirmPassword.getIntent(context, toInput()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun MissingScopeState.ScopeMissing.toInput() = ConfirmPasswordInput(
    userId = userId.id,
    missingScopes = missingScopes.map { it.value }
)
