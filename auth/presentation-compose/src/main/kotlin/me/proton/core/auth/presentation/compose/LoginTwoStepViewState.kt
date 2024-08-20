package me.proton.core.auth.presentation.compose

import me.proton.core.domain.entity.UserId

public sealed interface LoginTwoStepViewState {

    public sealed class UsernameInput : LoginTwoStepViewState {
        public data object Idle : UsernameInput()
        public data class Checking(val username: String) : UsernameInput()
        public data class Error(val message: String?) : UsernameInput()
        public data class ExternalAccountLoginNeeded(val username: String) : UsernameInput()
        public data object ExternalAccountNotSupported : UsernameInput()
    }

    public sealed class PasswordInput(public open val username: String) : LoginTwoStepViewState {
        public data class Idle(override val username: String) : PasswordInput(username)

        public data class Checking(
            override val username: String,
            val password: String
        ) : PasswordInput(username)

        public data class Error(
            override val username: String,
            val message: String?,
            val actionName: String?,
            val actionUrl: String?
        ) : PasswordInput(username)

        public data class LoggedIn(
            override val username: String,
            val userId: UserId,
            val nextStep: NextStep
        ) : PasswordInput(username)
    }

    public enum class NextStep {
        None,
        TwoPassMode,
        SecondFactor,
        ChooseAddress,
        ChangePassword
    }
}
