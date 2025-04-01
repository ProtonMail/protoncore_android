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

package me.proton.core.devicemigration.presentation.signin

import android.graphics.Bitmap
import androidx.compose.ui.unit.Dp
import me.proton.core.compose.effect.Effect

internal data class SignInStateHolder(
    val effect: Effect<SignInEvent>? = null,
    val state: SignInState
)

internal sealed interface SignInState {
    data object Loading : SignInState
    data class Idle(val qrCode: String, val generateBitmap: suspend (String, Dp) -> Bitmap) : SignInState
    data class UnrecoverableError(val onRetry: () -> Unit) : SignInState
    data object SuccessfullySignedIn : SignInState
}
