package me.proton.core.auth.domain.usecase.signup

import kotlinx.coroutines.flow.first
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.AccountWorkflowHandler
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage
import javax.inject.Inject

@ExcludeFromCoverage
class SetCreateAccountSuccess @Inject constructor(
    private val accountManager: AccountManager,
    private val accountWorkflowHandler: AccountWorkflowHandler
) {
    suspend operator fun invoke() {
        accountManager.getAccounts(AccountState.CreateAccountNeeded).first().forEach {
            accountWorkflowHandler.handleCreateAccountSuccess(it.userId)
        }
    }
}
