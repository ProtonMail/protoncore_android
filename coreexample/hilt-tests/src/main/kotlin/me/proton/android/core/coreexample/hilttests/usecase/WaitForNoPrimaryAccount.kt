package me.proton.android.core.coreexample.hilttests.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.account.domain.entity.Account
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import javax.inject.Inject
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val ACCOUNT_WAIT_MS = 30L * 1000
private const val WAIT_DELAY_MS = 250L

class WaitForNoPrimaryAccount @Inject constructor(private val accountManager: AccountManager) {
    operator fun invoke(timeout: Duration = ACCOUNT_WAIT_MS.milliseconds) = runBlocking {
        val account = waitForNoPrimaryAccount(timeout)
        assertNull(
            account,
            "Primary account is still accessible after $timeout (account: $account)."
        )
    }

    private suspend fun waitForNoPrimaryAccount(
        timeout: Duration
    ): Account? {
        suspend fun getAccount(): Account? = accountManager.getPrimaryAccount().first()
        var account: Account? = getAccount()
        withTimeoutOrNull(timeout) {
            while (true) {
                if (account == null) break
                delay(WAIT_DELAY_MS)
                account = getAccount()
            }
        }
        return account
    }
}