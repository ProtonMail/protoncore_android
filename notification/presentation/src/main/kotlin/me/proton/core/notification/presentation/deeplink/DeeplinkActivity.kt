/*
 * Copyright (c) 2023 Proton Technologies AG
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

import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Prepare [DeeplinkManager] intent handling on [ComponentActivity.onCreate].
 */
public fun DeeplinkManager.onActivityCreate(activity: ComponentActivity, savedInstanceState: Bundle?) {
    // Handle on Activity newly created, but not on configuration changes.
    if (savedInstanceState == null) {
        handle(activity.intent, activity)
    }

    // Handle on Activity onNewIntent.
    activity.addOnNewIntentListener { handle(it, activity) }
}
