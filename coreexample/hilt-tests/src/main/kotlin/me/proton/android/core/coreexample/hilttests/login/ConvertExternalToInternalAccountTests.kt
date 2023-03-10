package me.proton.android.core.coreexample.hilttests.login

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import me.proton.android.core.coreexample.BuildConfig
import me.proton.android.core.coreexample.Constants
import me.proton.android.core.coreexample.MainActivity
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.android.core.coreexample.di.ApplicationModule
import me.proton.android.core.coreexample.hilttests.di.MailApiClient
import me.proton.android.core.coreexample.hilttests.usecase.WaitForNoPrimaryAccount
import me.proton.android.core.coreexample.hilttests.usecase.WaitForPrimaryAccount
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.test.login.BaseConvertExternalToInternalAccountTests
import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.Product
import me.proton.core.network.domain.client.ExtraHeaderProvider
import me.proton.core.test.android.instrumented.ProtonTest
import me.proton.core.test.quark.Quark
import me.proton.core.test.quark.data.User
import me.proton.core.user.domain.UserManager
import org.junit.Rule
import javax.inject.Inject
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds

@HiltAndroidTest
@UninstallModules(ApplicationModule::class)
class ConvertExternalToInternalAccountTests : BaseConvertExternalToInternalAccountTests,
    ProtonTest(MainActivity::class.java) {
    override val quark: Quark =
        Quark.fromDefaultResources(Constants.QUARK_HOST, BuildConfig.PROXY_TOKEN)
    override lateinit var testUser: User
    override lateinit var testUsername: String

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

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

    @BeforeTest
    override fun prepare() {
        hiltRule.inject()
        extraHeaderProvider.addHeaders("X-Accept-ExtAcc" to "true")
        super.prepare()
    }

    override fun verifyLoggedOut() {
        waitForNoPrimaryAccount(timeout = 10.seconds)
    }

    override fun verifySuccessfulLogin() {
        val account = waitForPrimaryAccount()
        assertNotNull(account)

        val user = runBlocking { userManager.getUser(account.userId) }

        assertEquals(testUser.email, account.email)
        assertEquals(testUsername, user.name)
    }
}
