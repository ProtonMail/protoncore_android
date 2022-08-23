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

package me.proton.core.humanverification.presentation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.presentation.app.AppLifecycleObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HumanVerificationStateHandler @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val appLifecycleObserver: AppLifecycleObserver,
    private val humanVerificationManager: HumanVerificationManager
) {
    fun observe() {
        humanVerificationManager.observe(appLifecycleObserver.lifecycle)
            .onHumanVerificationNeeded { HumanVerificationOrchestrator.startHumanVerificationWorkflow(it, context) }
    }
}
