package me.proton.core.auth.presentation.compose.sso

public sealed interface PasswordFormError {
    public data object PasswordTooShort : PasswordFormError
    public data object PasswordTooCommon : PasswordFormError
    public data object PasswordsDoNotMatch : PasswordFormError
}
