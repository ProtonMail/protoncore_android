package me.proton.core.auth.presentation.compose

import me.proton.core.domain.entity.UserId

public sealed interface DeviceSecretViewState {
    public data object Close : DeviceSecretViewState
    public data object Loading : DeviceSecretViewState

    /**
     * When: No User Keys (no DeviceSecret).
     * Next step: Join organization and set backup password.
     */
    public data object FirstLogin : DeviceSecretViewState

    /**
     * When: User Keys exist, but invalid DeviceSecret.
     * Next step: Get a valid DeviceSecret.
     */
    public sealed interface InvalidSecret : DeviceSecretViewState {

        /**
         * When: No other logged-in Device.
         * Next step: Enter backup password or ask admin help.
         */
        public sealed interface NoDevice : InvalidSecret {
            public data object EnterBackupPassword : NoDevice
            public data object WaitingAdmin : NoDevice
        }

        /**
         * When: Other logged-in devices available.
         * Next step: Approval from other device, enter backup password or ask admin help.
         */
        public sealed interface OtherDevice : InvalidSecret {
            public data object WaitingMember : OtherDevice
        }
    }

    public data object DeviceRejected : DeviceSecretViewState

    public data class Error(val message: String?) : DeviceSecretViewState
    public data class Success(val userId: UserId) : DeviceSecretViewState
}
