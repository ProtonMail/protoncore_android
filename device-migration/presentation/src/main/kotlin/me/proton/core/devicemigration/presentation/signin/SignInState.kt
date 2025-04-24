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

public sealed class SignInState(public open val effect: Effect<SignInEvent>? = null) {
    public data object Loading : SignInState()
    public data class Idle(
        val errorMessage: String?,
        val qrCode: String,
        val generateBitmap: suspend (String, Dp) -> Bitmap
    ) : SignInState()

    public data class QrLoadFailure(val onRetry: (() -> Unit)) : SignInState()
    public data class Failure(val message: String, val onRetry: (() -> Unit)?) : SignInState()
    public data class SuccessfullySignedIn(override val effect: Effect<SignInEvent>) : SignInState(effect)
}
