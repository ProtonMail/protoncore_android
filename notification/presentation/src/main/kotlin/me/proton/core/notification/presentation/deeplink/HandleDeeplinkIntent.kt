/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.notification.presentation.deeplink

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import me.proton.core.presentation.app.ActivityProvider
import me.proton.core.util.kotlin.CoroutineScopeProvider
import me.proton.core.util.kotlin.DispatcherProvider
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

public class HandleDeeplinkIntent @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val activityProvider: ActivityProvider,
    private val coroutineScopeProvider: CoroutineScopeProvider,
    private val deeplinkManager: DeeplinkManager,
    private val dispatcherProvider: DispatcherProvider
) {
    @OptIn(FlowPreview::class)
    public operator fun invoke(intent: Intent) {
        coroutineScopeProvider.GlobalDefaultSupervisedScope.launch {
            withTimeout(10.seconds) {
                // Wait until activities settle:
                activityProvider.activityFlow
                    .debounce(600.milliseconds)
                    .first()

                withContext(dispatcherProvider.Main) {
                    deeplinkManager.handle(intent, appContext)
                }
            }
        }
    }
}
