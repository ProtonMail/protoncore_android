/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.challenge.presentation.compose

import android.content.ClipboardManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext

public object LocalClipManager {

    private val LocalClipManager = compositionLocalOf<ClipboardManager?> { null }

    public val current: ClipboardManager?
        @Composable
        get() = LocalClipManager.current
            ?: LocalContext.current.getSystemService(ClipboardManager::class.java)

    public infix fun provides(
        value: ClipboardManager?
    ): ProvidedValue<ClipboardManager?> = LocalClipManager.provides(value)

    @Composable
    public fun ClipboardManager.OnClipChangedDisposableEffect(block: (String) -> Unit) {
        val clipboardManager = LocalClipboardManager.current
        DisposableEffect(this) {
            val listener = ClipboardManager.OnPrimaryClipChangedListener {
                block(clipboardManager.getText()?.text ?: "")
            }
            addPrimaryClipChangedListener(listener)
            onDispose { removePrimaryClipChangedListener(listener) }
        }
    }
}
