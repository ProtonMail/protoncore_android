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
package me.proton.core.compose.activity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    val currentCounter = LocalScreenOnCounter.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        currentCounter.value++
        onDispose {
            if (--currentCounter.value <= 0) {
                currentView.keepScreenOn = false
            }
        }
    }
}

private val LocalScreenOnCounter = compositionLocalOf { mutableStateOf(0) }
