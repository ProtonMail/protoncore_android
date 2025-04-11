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

package me.proton.core.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import me.proton.core.presentation.utils.launchOnScreenView

@Composable
fun LaunchOnScreenView(enqueue: () -> Unit) {
    val registryOwner = LocalSavedStateRegistryOwner.current
    LaunchedEffect(registryOwner) {
        registryOwner.launchOnScreenView(registryOwner.savedStateRegistry) {
            enqueue()
        }
    }
}
