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

package me.proton.core.auth.presentation

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.entity.AccountType
import me.proton.core.auth.presentation.entity.AddressesResult
import me.proton.core.auth.presentation.entity.LoginInput
import me.proton.core.auth.presentation.entity.LoginResult
import me.proton.core.auth.presentation.entity.ScopeResult
import me.proton.core.auth.presentation.entity.SecondFactorInput
import me.proton.core.auth.presentation.entity.TwoPassModeInput
import me.proton.core.auth.presentation.entity.UserResult
import me.proton.core.auth.presentation.ui.CreateAddressInput
import me.proton.core.auth.presentation.ui.StartUsernameChooseForAccountUpgrade
import me.proton.core.auth.presentation.ui.StartLogin
import me.proton.core.auth.presentation.ui.StartSecondFactor
import me.proton.core.auth.presentation.ui.StartTwoPassMode
import me.proton.core.auth.presentation.ui.StartAccountUpgrade
import me.proton.core.auth.presentation.ui.UpgradeInput
import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.presentation.entity.HumanVerificationInput
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.ui.StartHumanVerification
import me.proton.core.network.domain.humanverification.HumanVerificationDetails
import me.proton.core.network.domain.session.SessionId
import javax.inject.Inject

class AuthOrchestrator @Inject constructor(
    private val accountWorkflowHandler: AccountWorkflowHandler
) {

    // region result launchers
    private var loginWorkflowLauncher: ActivityResultLauncher<LoginInput>? = null
    private var secondFactorWorkflowLauncher: ActivityResultLauncher<SecondFactorInput>? = null
    private var twoPassModeWorkflowLauncher: ActivityResultLauncher<TwoPassModeInput>? = null
    private var humanWorkflowLauncher: ActivityResultLauncher<HumanVerificationInput>? = null
    private var createAddressLauncher: ActivityResultLauncher<CreateAddressInput>? = null
    private var upgradeLauncher: ActivityResultLauncher<UpgradeInput>? = null
    // endregion

    private var onLoginResultListener: (result: LoginResult?) -> Unit = {}
    private var onUserResultListener: (result: UserResult?) -> Unit = {}
    private var onScopeResultListener: (result: ScopeResult?) -> Unit = {}
    private var onHumanVerificationResultListener: (result: HumanVerificationResult?) -> Unit = {}

    fun setOnLoginResult(block: (result: LoginResult?) -> Unit) {
        onLoginResultListener = block
    }

    fun setOnUserResult(block: (result: UserResult?) -> Unit) {
        onUserResultListener = block
    }

    fun setOnScopeResult(block: (result: ScopeResult?) -> Unit) {
        onScopeResultListener = block
    }

    fun setOnHumanVerificationResult(block: (result: HumanVerificationResult?) -> Unit) {
        onHumanVerificationResultListener = block
    }

    // region private module functions
    private fun registerLoginResult(
        context: ComponentActivity
    ): ActivityResultLauncher<LoginInput> =
        context.registerForActivityResult(
            StartLogin()
        ) { result ->
            result?.let {
                when {
                    it.session.isSecondFactorNeeded -> {
                        startSecondFactorWorkflow(
                            it.session.sessionId,
                            it.session.isTwoPassModeNeeded,
                            it.password,
                            it.requiredAccountType
                        )
                    }
                    it.session.isTwoPassModeNeeded -> {
                        startTwoPassModeWorkflow(SessionId(it.session.sessionId), it.requiredAccountType)
                    }
                    else -> it.user?.let { user ->
                        onUserProvided(context, user, result.session.sessionId, result.requiredAccountType)
                    }
                }
            }
            onLoginResultListener(result)
        }

    private fun onUserProvided(
        context: ComponentActivity,
        user: UserResult,
        sessionId: String,
        requiredAccountType: AccountType
    ) {
        user.addresses.let { addresses ->
            if (!addresses.satisfiesAccountType(requiredAccountType)) {
                startUpgradeAccountWorkflow(addresses, user, sessionId)
            } else {
                context.lifecycleScope.launch {
                    accountWorkflowHandler.handleAccountReady(userId = UserId(user.id))
                }
                onUserResultListener(user)
            }
        }
    }

    private fun registerTwoPassModeResult(
        context: ComponentActivity
    ): ActivityResultLauncher<TwoPassModeInput> =
        context.registerForActivityResult(
            StartTwoPassMode()
        ) { result ->
            result?.let { onUserProvided(context, it.user, it.sessionId, it.requiredAccountType) }
        }

    private fun registerSecondFactorResult(
        context: ComponentActivity
    ): ActivityResultLauncher<SecondFactorInput> =
        context.registerForActivityResult(
            StartSecondFactor()
        ) { result ->
            result?.let {
                if (it.isTwoPassModeNeeded) {
                    startTwoPassModeWorkflow(SessionId(it.scope.sessionId), it.requiredAccountType)
                } else {
                    onUserResultListener(requireNotNull(it.user))
                }
                onScopeResultListener(it.scope)
            }
        }

    private fun registerHumanVerificationResult(
        context: ComponentActivity
    ): ActivityResultLauncher<HumanVerificationInput> =
        context.registerForActivityResult(
            StartHumanVerification()
        ) { result ->
            result?.let {
                context.lifecycleScope.launch {
                    if (!it.tokenType.isNullOrBlank() && !it.tokenCode.isNullOrBlank()) {
                        accountWorkflowHandler.handleHumanVerificationSuccess(
                            sessionId = SessionId(it.sessionId),
                            tokenType = it.tokenType!!,
                            tokenCode = it.tokenCode!!
                        )
                    } else {
                        accountWorkflowHandler.handleHumanVerificationFailed(
                            sessionId = SessionId(it.sessionId)
                        )
                    }
                }
                onHumanVerificationResultListener(it)
            }
        }

    private fun registerCreateAddressResult(
        context: ComponentActivity
    ): ActivityResultLauncher<CreateAddressInput> =
        context.registerForActivityResult(
            StartUsernameChooseForAccountUpgrade()
        ) { result ->
            result?.let {
                context.lifecycleScope.launch {
                    accountWorkflowHandler.handleAccountReady(userId = UserId(it.id))
                }
                onUserResultListener(result)
            }
        }

    private fun registerUpgradeUsernameOnlyResult(
        context: ComponentActivity
    ): ActivityResultLauncher<UpgradeInput> =
        context.registerForActivityResult(
            StartAccountUpgrade()
        ) { result ->
            result?.let {
                context.lifecycleScope.launch {
                    accountWorkflowHandler.handleAccountReady(userId = UserId(it.id))
                }
                onUserResultListener(result)
            }
        }

    private fun startSecondFactorWorkflow(
        sessionId: String,
        isTwoPassModeNeeded: Boolean,
        password: ByteArray,
        requiredAccountType: AccountType
    ) {
        secondFactorWorkflowLauncher?.launch(
            SecondFactorInput(sessionId, isTwoPassModeNeeded, requiredAccountType, password)
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    private fun startCreateAddressWorkflow(input: CreateAddressInput) {
        createAddressLauncher?.launch(input)
            ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    private fun startUpgradeUsernameWorkflow(input: UpgradeInput) {
        upgradeLauncher?.launch(input)
            ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    private fun startUpgradeAccountWorkflow(
        addressResult: AddressesResult,
        user: UserResult,
        sessionId: String
    ) {
        if (addressResult.currentAccountType() == AccountType.Username) {
            startUpgradeUsernameWorkflow(
                UpgradeInput(
                    sessionId = SessionId(sessionId),
                    user = user,
                    username = user.name ?: "" // should be checked
                )
            )
        } else {
            startCreateAddressWorkflow(
                CreateAddressInput(
                    sessionId = SessionId(sessionId),
                    externalEmail = user.email, // this should be checked in real world, maybe username?
                    user = user
                )
            )
        }
    }
    // endregion

    // region public API
    /**
     * Register all needed workflow for internal usage.
     *
     * Note: This function have to be called [ComponentActivity.onCreate]] before [ComponentActivity.onResume].
     */
    fun register(context: ComponentActivity) {
        loginWorkflowLauncher = registerLoginResult(context)
        humanWorkflowLauncher = registerHumanVerificationResult(context)
        secondFactorWorkflowLauncher = registerSecondFactorResult(context)
        twoPassModeWorkflowLauncher = registerTwoPassModeResult(context)
        createAddressLauncher = registerCreateAddressResult(context)
        upgradeLauncher = registerUpgradeUsernameOnlyResult(context)
    }

    /**
     * Starts the Login workflow.
     */
    fun startLoginWorkflow(requiredAccountType: AccountType) {
        loginWorkflowLauncher?.launch(
            LoginInput(requiredAccountType)
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    /**
     * Start a TwoPassMode workflow.
     */
    fun startTwoPassModeWorkflow(sessionId: SessionId, requiredAccountType: AccountType) {
        twoPassModeWorkflowLauncher?.launch(
            TwoPassModeInput(sessionId.id, requiredAccountType)
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
    }

    /**
     * Start a Human Verification workflow.
     */
    fun startHumanVerificationWorkflow(sessionId: SessionId, details: HumanVerificationDetails?) {
        humanWorkflowLauncher?.launch(
            HumanVerificationInput(
                sessionId.id,
                details?.verificationMethods?.map { it.value },
                details?.captchaVerificationToken
            )
        ) ?: throw IllegalStateException("You must call register before any start workflow function!")
    }
    // endregion
}

fun AuthOrchestrator.onUserResult(
    block: (result: UserResult?) -> Unit
): AuthOrchestrator {
    setOnUserResult { block(it) }
    return this
}

fun AuthOrchestrator.onScopeResult(
    block: (result: ScopeResult?) -> Unit
): AuthOrchestrator {
    setOnScopeResult { block(it) }
    return this
}

fun AuthOrchestrator.onLoginResult(
    block: (result: LoginResult?) -> Unit
): AuthOrchestrator {
    setOnLoginResult { block(it) }
    return this
}

fun AuthOrchestrator.onHumanVerificationResult(
    block: (result: HumanVerificationResult?) -> Unit
): AuthOrchestrator {
    setOnHumanVerificationResult { block(it) }
    return this
}
