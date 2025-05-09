package me.proton.core.auth.presentation.compose

import me.proton.core.domain.entity.UserId

public sealed class DeviceSecretViewState(public open val email: String?) {
    public data class Close(override val email: String?) : DeviceSecretViewState(email)
    public data class Loading(override val email: String?) : DeviceSecretViewState(email)

    /**
     * When: No User Keys (no DeviceSecret).
     * Next step: Join organization and set backup password.
     */
    public data class FirstLogin(override val email: String?, val userId: UserId) : DeviceSecretViewState(email)

    /**
     * When: User Keys exist, but invalid DeviceSecret.
     * Next step: Get a valid DeviceSecret.
     */
    public sealed class InvalidSecret(override val email: String?) : DeviceSecretViewState(email) {

        /**
         * When: No other logged-in Device.
         * Next step: Enter backup password or ask admin help.
         */
        public sealed class NoDevice(override val email: String?) : InvalidSecret(email) {
            public data class BackupPassword(override val email: String?) : NoDevice(email)
            public data class WaitingAdmin(override val email: String?) : NoDevice(email)
            public data class RequireAdmin(override val email: String?) : NoDevice(email)
        }

        /**
         * When: Other logged-in devices available.
         * Next step: Approval from other device, enter backup password or ask admin help.
         */
        public sealed class OtherDevice(override val email: String?) : InvalidSecret(email) {
            public data class WaitingMember(override val email: String?) : OtherDevice(email)
        }
    }

    /**
     * When: After Member/Admin reject.
     * Next step: Logout.
     */
    public data class DeviceRejected(override val email: String?) : DeviceSecretViewState(email)

    /**
     * When: After Admin granted.
     * Next step: Change password.
     */
    public data class DeviceGranted(override val email: String?) : DeviceSecretViewState(email)

    /**
     * When: After Admin approval / DeviceGranted.
     * Next step: Update password, Logged in.
     */
    public data class ChangePassword(override val email: String?, val userId: UserId) : DeviceSecretViewState(email)

    public data class Error(
        override val email: String?,
        val message: String?
    ) : DeviceSecretViewState(email)

    public data class Success(
        override val email: String?,
        val userId: UserId
    ) : DeviceSecretViewState(email)
}
