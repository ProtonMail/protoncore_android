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

package me.proton.core.devicemigration.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import me.proton.core.presentation.ui.ProtonActivity

public class TargetDeviceMigrationActivity : ProtonActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // TODO
            Text(text = "TargetDeviceMigrationActivity")
        }
    }

    // TODO publish result, specifically when the user has been logged in (TargetDeviceMigrationResult.SignedIn)

    internal companion object {
        const val ARG_RESULT = "result"
    }
}
