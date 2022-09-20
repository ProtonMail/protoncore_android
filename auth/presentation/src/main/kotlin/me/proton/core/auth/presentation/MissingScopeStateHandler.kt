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

package me.proton.core.auth.presentation

import me.proton.core.network.domain.scopes.MissingScopeListener
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.presentation.app.AppLifecycleObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissingScopeStateHandler @Inject constructor(
    private val activityProvider: ActivityProvider,
    private val appLifecycleObserver: AppLifecycleObserver,
    private val missingScopeListener: MissingScopeListener
) {
    fun observe() {
        missingScopeListener
            .observe(appLifecycleObserver.lifecycle)
            .onConfirmPasswordNeeded {
                activityProvider.lastResumed?.let { activity ->
                    it.startConfirmPasswordWorkflow(activity)
                }
            }
    }
}
