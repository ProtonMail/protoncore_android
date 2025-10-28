/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.auth.domain.usecase

import me.proton.core.account.domain.entity.AccountType
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.auth.domain.LogTag
import me.proton.core.auth.domain.entity.BillingDetails
import me.proton.core.auth.domain.entity.EncryptedAuthSecret
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.extension.getPurchaseOrNull
import me.proton.core.payment.domain.features.IsOmnichannelEnabled
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.payment.domain.usecase.FindGooglePurchaseForPaymentOrderId
import me.proton.core.payment.domain.usecase.PollPaymentTokenStatus
import me.proton.core.payment.domain.usecase.CreatePaymentTokenForGooglePurchase
import me.proton.core.plan.domain.usecase.PerformSubscribe
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.util.kotlin.CoreLogger
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

/**
 * Performs the account check after logging in to determine what actions are needed.
 */
class PostLoginAccountSetup @Inject constructor(
    private val accountWorkflow: AccountWorkflowHandler,
    private val createPaymentToken: CreatePaymentTokenForGooglePurchase,
    private val isOmnichannelEnabled: IsOmnichannelEnabled,
    private val optionalFindGooglePurchase: Optional<FindGooglePurchaseForPaymentOrderId>,
    private val performSubscribe: PerformSubscribe,
    private val pollPaymentTokenStatus: PollPaymentTokenStatus,
    private val purchaseRepository: PurchaseRepository,
    private val sessionManager: SessionManager,
    private val setupAccountCheck: SetupAccountCheck,
    private val setupExternalAddressKeys: SetupExternalAddressKeys,
    private val setupInternalAddress: SetupInternalAddress,
    private val setupPrimaryKeys: SetupPrimaryKeys,
    private val unlockUserPrimaryKey: UnlockUserPrimaryKey,
    private val userCheck: UserCheck,
    private val userManager: UserManager
) {
    sealed class Result {
        sealed class Error : Result() {
            data class UnlockPrimaryKeyError(val error: UserManager.UnlockResult.Error) : Error()
            data class UserCheckError(val error: UserCheckResult.Error) : Error()
        }

        sealed class Need : Result() {
            data class SecondFactor(val userId: UserId) : Need()
            data class TwoPassMode(val userId: UserId) : Need()
            data class ChangePassword(val userId: UserId) : Need()
            data class ChooseUsername(val userId: UserId) : Need()
            data class DeviceSecret(val userId: UserId) : Need()
        }

        data class AccountReady(val userId: UserId) : Result()
    }

    sealed class UserCheckResult {
        object Success : UserCheckResult()
        data class Error(val localizedMessage: String, val action: UserCheckAction? = null) : UserCheckResult()
    }

    interface UserCheck {
        /**
         * Check if [User] match criteria to continue the setup account process.
         */
        suspend operator fun invoke(user: User): UserCheckResult
    }

    suspend operator fun invoke(
        userId: UserId,
        encryptedPassword: EncryptedString,
        requiredAccountType: AccountType,
        isSecondFactorNeeded: Boolean,
        isTwoPassModeNeeded: Boolean,
        temporaryPassword: Boolean,
        onSetupSuccess: (suspend () -> Unit)? = null,
        billingDetails: BillingDetails? = null,
        internalAddressDomain: String? = null
    ): Result {
        return invoke(
            userId = userId,
            encryptedAuthSecret = EncryptedAuthSecret.Password(encryptedPassword),
            requiredAccountType = requiredAccountType,
            isSecondFactorNeeded = isSecondFactorNeeded,
            isTwoPassModeNeeded = isTwoPassModeNeeded,
            temporaryPassword = temporaryPassword,
            onSetupSuccess = onSetupSuccess,
            billingDetails = billingDetails,
            internalAddressDomain = internalAddressDomain,
        )
    }

    suspend operator fun invoke(
        userId: UserId,
        encryptedAuthSecret: EncryptedAuthSecret,
        requiredAccountType: AccountType,
        isSecondFactorNeeded: Boolean,
        isTwoPassModeNeeded: Boolean,
        temporaryPassword: Boolean,
        onSetupSuccess: (suspend () -> Unit)? = null,
        billingDetails: BillingDetails? = null,
        internalAddressDomain: String? = null
    ): Result {
        // Flows not using PurchaseStateHandler pass billingDetails.
        subscribeAnyPendingBilling(billingDetails, userId)
        // Flows using PurchaseStateHandler drop a Purchase off.
        subscribeAnyPendingPurchase(userId)

        // If SecondFactorNeeded, we cannot proceed without.
        if (isSecondFactorNeeded) {
            return Result.Need.SecondFactor(userId)
        }

        return when (setupAccountCheck(userId, isTwoPassModeNeeded, requiredAccountType, temporaryPassword)) {
            is SetupAccountCheck.Result.TwoPassNeeded -> {
                accountWorkflow.handleTwoPassModeNeeded(userId)
                Result.Need.TwoPassMode(userId)
            }
            is SetupAccountCheck.Result.ChangePasswordNeeded -> {
                accountWorkflow.handleAccountDisabled(userId)
                Result.Need.ChangePassword(userId)
            }
            is SetupAccountCheck.Result.ChooseUsernameNeeded -> {
                accountWorkflow.handleCreateAddressNeeded(userId)
                Result.Need.ChooseUsername(userId)
            }
            is SetupAccountCheck.Result.SetupPrimaryKeysNeeded -> {
                val encryptedPassword = encryptedAuthSecret as? EncryptedAuthSecret.Password
                if (encryptedPassword != null) {
                    setupPrimaryKeys.invoke(
                        userId,
                        encryptedPassword.password,
                        requiredAccountType,
                        internalAddressDomain
                    )
                    unlockUserPrimaryKey(
                        userId,
                        encryptedAuthSecret,
                        onSetupSuccess
                    )
                } else {
                    Result.Error.UnlockPrimaryKeyError(UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase)
                }
            }
            is SetupAccountCheck.Result.SetupExternalAddressKeysNeeded -> {
                unlockUserPrimaryKey(
                    userId,
                    encryptedAuthSecret,
                    onSetupSuccess
                ) {
                    setupExternalAddressKeys.invoke(userId)
                }
            }
            is SetupAccountCheck.Result.SetupInternalAddressNeeded -> {
                unlockUserPrimaryKey(
                    userId,
                    encryptedAuthSecret,
                    onSetupSuccess
                ) {
                    setupInternalAddress.invoke(userId, internalAddressDomain)
                }
            }
            is SetupAccountCheck.Result.NoSetupNeeded -> {
                unlockUserPrimaryKey(
                    userId,
                    encryptedAuthSecret,
                    onSetupSuccess
                )
            }
        }
    }

    private suspend fun subscribeAnyPendingBilling(
        billingDetails: BillingDetails?,
        userId: UserId
    ) {
        if (billingDetails != null) runCatching {
            performSubscribe(
                userId = userId,
                cycle = billingDetails.cycle,
                planNames = listOf(billingDetails.planName),
                paymentToken = billingDetails.token
            )
        }.onFailure {
            CoreLogger.e(LogTag.PERFORM_SUBSCRIBE, it)
        }
    }

    private suspend fun subscribeAnyPendingPurchase(userId: UserId) {
        purchaseRepository.getPurchaseOrNull(PurchaseState.Purchased)?.let { purchase ->
            runCatching {
                val findGooglePurchase = requireNotNull(optionalFindGooglePurchase.getOrNull())
                val googlePurchase = requireNotNull(findGooglePurchase(purchase.paymentOrderId))

                val tokenResponse = createPaymentToken(
                    googleProductId = googlePurchase.productIds.first(),
                    purchase = googlePurchase,
                    userId = userId
                )

                if (isOmnichannelEnabled(userId)) {
                    pollPaymentTokenStatus(userId, tokenResponse.token)
                }

                performSubscribe(
                    cycle =  SubscriptionCycle.map[purchase.planCycle] ?: SubscriptionCycle.OTHER,
                    paymentToken = tokenResponse.token,
                    planNames = listOf(purchase.planName),
                    userId = userId
                )
            }.onSuccess { result ->
                purchaseRepository.upsertPurchase(purchase.copy(purchaseState = PurchaseState.Subscribed))
            }.onFailure { error ->
                CoreLogger.e(LogTag.PERFORM_SUBSCRIBE, error)
            }
        }
    }

    private suspend fun unlockUserPrimaryKey(
        userId: UserId,
        secret: EncryptedAuthSecret,
        onSetupSuccess: (suspend () -> Unit)?,
        onUnlockSuccess: (suspend () -> Unit)? = null,
    ): Result {
        return when (val result = unlockUserPrimaryKey.invoke(userId, secret)) {
            is UserManager.UnlockResult.Success -> {
                // Invoke unlock success action.
                onUnlockSuccess?.invoke()
                // Refresh scopes.
                sessionManager.refreshScopes(checkNotNull(sessionManager.getSessionId(userId)))
                // First get the User to invoke UserCheck.
                val user = userManager.getUser(userId, refresh = true)
                when (val userCheckResult = userCheck.invoke(user)) {
                    is UserCheckResult.Error -> {
                        // Disable account and prevent login.
                        accountWorkflow.handleAccountDisabled(userId)
                        return Result.Error.UserCheckError(userCheckResult)
                    }
                    is UserCheckResult.Success -> {
                        // Invoke setup success action.
                        onSetupSuccess?.invoke()
                        // Last step, change account state to Ready.
                        accountWorkflow.handleAccountReady(userId)
                        Result.AccountReady(userId)
                    }
                }
            }
            is UserManager.UnlockResult.Error.NoPrimaryKey,
            is UserManager.UnlockResult.Error.NoKeySaltsForPrimaryKey -> {
                // Unrecoverable -> Disable account.
                accountWorkflow.handleUnlockFailed(userId)
                Result.Error.UnlockPrimaryKeyError(result as UserManager.UnlockResult.Error)
            }
            is UserManager.UnlockResult.Error.PrimaryKeyInvalidPassphrase -> {
                // Recoverable -> Let the User retry.
                Result.Error.UnlockPrimaryKeyError(result)
            }
        }
    }
}
