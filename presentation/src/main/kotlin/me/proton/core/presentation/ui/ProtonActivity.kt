/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.core.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.proton.core.presentation.utils.OnUiComponentCreatedListener
import me.proton.core.presentation.utils.UiComponent

/**
 * Base Proton Activity from which all project activities should extend.
 */
abstract class ProtonActivity : AppCompatActivity(), OnUiComponentCreatedListener {

    protected var activityInForeground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onUiComponentCreated(this, this, this, UiComponent.UiActivity(this))
    }

    override fun onPause() {
        super.onPause()
        activityInForeground = false
    }

    override fun onResume() {
        super.onResume()
        activityInForeground = true
    }
}
