package me.proton.android.core.coreexample.hilttests.login

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import me.proton.android.core.coreexample.MainActivity
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.android.core.coreexample.di.ApplicationModule
import me.proton.android.core.coreexample.hilttests.di.MailApiClient
import me.proton.android.core.coreexample.hilttests.rule.LogsRule
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.test.BaseConvertExternalToInternalAccountTests
import me.proton.core.auth.test.usecase.WaitForNoPrimaryAccount
import me.proton.core.auth.test.usecase.WaitForPrimaryAccount
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.test.rule.ProtonRule
import me.proton.core.test.rule.extension.protonActivityScenarioRule
import me.proton.core.user.domain.UserManager
import org.junit.Rule
import javax.inject.Inject
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
class ConvertExternalToInternalAccountTests : BaseConvertExternalToInternalAccountTests() {

    @get:Rule
    override val protonRule: ProtonRule = protonActivityScenarioRule<MainActivity>()

    @BeforeTest
    fun prepare() {
        extraHeaderProvider.addHeaders("X-Accept-ExtAcc" to "true")
    }

    override fun loggedIn(username: String) {
        val account = waitForPrimaryAccount()
        assertNotNull(account)

        val user = runBlocking { userManager.getUser(account.userId) }

        assertEquals(protonRule.testDataRule.mainTestUser?.externalEmail, account.email)
        assertEquals(protonRule.testDataRule.mainTestUser?.name, user.name)
    }

    @get:Rule
    val logsRule = LogsRule()

    @BindValue
    val apiClient: CoreExampleApiClient = MailApiClient

    @BindValue
    val appStore: AppStore = AppStore.GooglePlay

    @BindValue
    val product: Product = Product.Mail

    @BindValue
    val accountType: AccountType = AccountType.Internal

    @Inject
    lateinit var extraHeaderProvider: ExtraHeaderProvider

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var waitForPrimaryAccount: WaitForPrimaryAccount

    @Inject
    lateinit var waitForNoPrimaryAccount: WaitForNoPrimaryAccount
}
