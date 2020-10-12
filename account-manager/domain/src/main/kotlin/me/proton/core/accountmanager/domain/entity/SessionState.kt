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

package me.proton.core.accountmanager.domain.entity

enum class SessionState {
    /**
     * A second factor is needed.
     *
     * Note: Another [Account] or [Session] could be progressing in a workflow.
     *
     * @see [SecondFactorStarting]
     * @see [SecondFactorInProgress].
     */
    SecondFactorNeeded,

    /**
     * The second factor is starting.
     *
     * Client should call [startSecondFactorWorkflow] asap.
     */
    SecondFactorStarting,

    /**
     * The second factor is in progress.
     *
     * Note: Always followed by either [SecondFactorSuccess] or [SecondFactorFailed].
     */
    SecondFactorInProgress,

    /**
     * The second factor has been successful.
     *
     * Note: Usually followed by [Authenticated].
     */
    SecondFactorSuccess,

    /**
     * The second factor has failed.
     *
     * Client should consider calling [startSecondFactorWorkflow] in conjunction with [hasWorkflowProgressing].
     */
    SecondFactorFailed,

    /**
     * A human verification is needed.
     *
     * Note: Another [Account] or [Session] could be progressing in a workflow.
     *
     * @see [HumanVerificationStarting]
     * @see [HumanVerificationInProgress].
     */
    HumanVerificationNeeded,

    /**
     * The human verification is starting.
     *
     * Client should call [startHumanVerificationWorkflow] asap.
     */
    HumanVerificationStarting,

    /**
     * The human verification is in progress.
     *
     * Note: Always followed by either [HumanVerificationSuccess] or [HumanVerificationFailed].
     */
    HumanVerificationInProgress,

    /**
     * The human verification has been successful.
     *
     * Note: Usually followed by [Authenticated].
     */
    HumanVerificationSuccess,

    /**
     * The human verification has failed.
     *
     * Client should consider calling [startHumanVerificationWorkflow] in conjunction with [hasWorkflowProgressing].
     */
    HumanVerificationFailed,

    /**
     * This [Session] is fully authenticated, no additional step needed.
     */
    Authenticated,

    /**
     * This [Session] is no longer valid.
     *
     * Client should consider calling [startLoginWorkflow] in conjunction with [hasWorkflowProgressing].
     */
    TokenRefreshFailed,

    /**
     * This [Session] is no longer valid.
     *
     * Client should consider calling [startLoginWorkflow] in conjunction with [hasWorkflowProgressing].
     */
    ForceLogout
}
