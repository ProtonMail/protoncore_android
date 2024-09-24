/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.auth.presentation.compose.sso.backuppassword.setup

public sealed class BackupPasswordSetupState(
    public open val data: BackupPasswordSetupData
) {
    public data class Idle(
        override val data: BackupPasswordSetupData
    ) : BackupPasswordSetupState(data)

    public data class Loading(
        override val data: BackupPasswordSetupData
    ) : BackupPasswordSetupState(data)

    public data class Error(
        override val data: BackupPasswordSetupData,
        val message: String?
    ) : BackupPasswordSetupState(data)

    public data class FormError(
        override val data: BackupPasswordSetupData,
        val cause: BackupPasswordSetupFormError
    ) : BackupPasswordSetupState(data)

    public data class Success(
        override val data: BackupPasswordSetupData
    ) : BackupPasswordSetupState(data)
}

internal fun BackupPasswordSetupState.formErrorOrNull(): BackupPasswordSetupFormError? =
    (this as? BackupPasswordSetupState.FormError)?.cause

internal fun BackupPasswordSetupState.isPasswordTooShort(): Boolean =
    formErrorOrNull() == BackupPasswordSetupFormError.PasswordTooShort

internal fun BackupPasswordSetupState.arePasswordsNotMatching(): Boolean =
    formErrorOrNull() == BackupPasswordSetupFormError.PasswordsDoNotMatch

public sealed interface BackupPasswordSetupFormError {
    public data object PasswordTooShort : BackupPasswordSetupFormError
    public data object PasswordsDoNotMatch : BackupPasswordSetupFormError
}
