/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.account.domain.entity

enum class AccountState {
    /**
     * State emitted if this [Account] need more step(s) to be [Ready] to use.
     */
    NotReady,

    /**
     * State emitted if this [Account] need a migration to be [Ready] to use.
     */
    MigrationNeeded,

    /**
     * A two pass mode is needed.
     *
     * Note: Usually followed by either [TwoPassModeSuccess] or [TwoPassModeFailed].
     *
     * @see [TwoPassModeSuccess]
     * @see [TwoPassModeFailed].
     */
    TwoPassModeNeeded,

    /**
     * The two pass mode has been successful.
     */
    TwoPassModeSuccess,

    /**
     * The two pass mode has failed.
     *
     * Note: Client should consider calling [startLoginWorkflow].
     */
    TwoPassModeFailed,

    /**
     * Choose Username and Create an Address is needed.
     *
     * Note: Usually followed by either [CreateAddressSuccess] or [CreateAddressFailed].
     *
     * @see [CreateAddressSuccess]
     * @see [CreateAddressFailed].
     */
    CreateAddressNeeded,

    /**
     * The address creation has been successful.
     */
    CreateAddressSuccess,

    /**
     * The address creation has failed.
     */
    CreateAddressFailed,

    /**
     * Create an Account is needed (e.g. current account is not enough/supported anymore).
     *
     * Note: Usually followed by either [CreateAccountSuccess] or [CreateAccountFailed].
     *
     * @see [CreateAccountSuccess]
     * @see [CreateAccountFailed].
     */
    CreateAccountNeeded,

    /**
     * The account creation has been successful.
     */
    CreateAccountSuccess,

    /**
     * The account creation has failed.
     */
    CreateAccountFailed,

    /**
     * A device secret is needed.
     *
     * Note: Usually followed by either [DeviceSecretSuccess] or [DeviceSecretFailed].
     *
     * @see [DeviceSecretSuccess]
     * @see [DeviceSecretFailed].
     */
    DeviceSecretNeeded,

    /**
     * The device secret step has been successful.
     */
    DeviceSecretSuccess,

    /**
     * The device secret step has failed.
     */
    DeviceSecretFailed,

    /**
     * Unlock User primary key has failed.
     */
    UnlockFailed,

    /**
     * User key check has failed.
     */
    UserKeyCheckFailed,

    /**
     * User Address key check has failed.
     */
    UserAddressKeyCheckFailed,

    /**
     * The [Account] is ready to use and contains a valid [Session].
     */
    Ready,

    /**
     * The [Account] has been disabled and do not contains valid [Session].
     */
    Disabled,

    /**
     * The [Account] has been removed from persistence.
     *
     * Note: Usually used by Client to clean up [Account] related resources.
     */
    Removed
}
