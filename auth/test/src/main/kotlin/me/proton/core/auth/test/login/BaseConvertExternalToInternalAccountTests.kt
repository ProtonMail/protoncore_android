package me.proton.core.auth.test.login

import me.proton.core.auth.test.robot.AddAccountRobot
import me.proton.core.auth.test.robot.ChooseInternalAddressRobot
import me.proton.core.auth.test.robot.TwoPassRobot
import me.proton.core.test.quark.Quark
import me.proton.core.test.quark.data.User
import me.proton.core.util.kotlin.random
import kotlin.test.BeforeTest
import kotlin.test.Test

public interface BaseConvertExternalToInternalAccountTests {
    public val quark: Quark
    public var testUser: User
    public var testUsername: String

    public fun verifyLoggedOut()
    public fun verifySuccessfulLogin()

    @BeforeTest
    public fun prepare() {
        quark.jailUnban()
    }

    @Test
    public fun happyPath() {
        // GIVEN
        testUsername = User.randomUsername()
        testUser = User(
            name = "",
            email = "${testUsername}@external-domain.test",
            isExternal = true
        )
        quark.userCreate(testUser, Quark.CreateAddress.WithKey())

        // WHEN
        AddAccountRobot
            .goToLogin()
            .fillUsername(testUser.email)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                continueButtonIsEnabled()
                domainInputDisplayed()
                usernameInputIsFilled(testUsername)
            }
            .selectAlternativeDomain()
            .selectPrimaryDomain()
            .next()

        // THEN
        verifySuccessfulLogin()
    }

    @Test
    public fun externalAccountWithAddressButNoAddressKey() {
        // GIVEN
        testUsername = User.randomUsername()
        testUser = User(
            name = "",
            email = "${testUsername}@external-domain.test",
            isExternal = true
        )
        quark.userCreate(testUser, Quark.CreateAddress.NoKey)

        // WHEN
        AddAccountRobot
            .goToLogin()
            .fillUsername(testUser.email)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                continueButtonIsEnabled()
                domainInputDisplayed()
                usernameInputIsFilled(testUsername)
            }
            .next()

        // THEN
        verifySuccessfulLogin()
    }

    @Test
    public fun accountWithTwoPasswordMode() {
        // GIVEN
        val passphrase = "passphrase"
        testUsername = User.randomUsername()
        testUser = User(
            name = "",
            email = "${testUsername}@external-domain.test",
            isExternal = true,
            passphrase = passphrase
        )
        quark.userCreate(testUser, Quark.CreateAddress.WithKey())

        // WHEN
        AddAccountRobot
            .goToLogin()
            .fillUsername(testUser.email)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                continueButtonIsEnabled()
                domainInputDisplayed()
                usernameInputIsFilled(testUsername)
            }
            .next()

        TwoPassRobot
            .fillMailboxPassword(passphrase)
            .unlock()

        // THEN
        verifySuccessfulLogin()
    }

    @Test
    public fun accountWithUnavailableUsername() {
        // GIVEN
        val domain = String.random()
        testUsername = "proton_core_$domain"
        testUser = User(
            name = "",
            email = "free@${domain}.test",
            isExternal = true
        )
        quark.userCreate(testUser, Quark.CreateAddress.WithKey())

        // WHEN
        AddAccountRobot
            .goToLogin()
            .fillUsername(testUser.email)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                continueButtonIsEnabled()
                domainInputDisplayed()
                usernameInputIsEmpty()
            }
            .fillUsername(testUsername)
            .next()

        // THEN
        verifySuccessfulLogin()
    }

    @Test
    public fun chooseInternalAddressIsClosed() {
        // GIVEN
        val domain = String.random()
        testUsername = "proton_core_$domain"
        testUser = User(
            name = "",
            email = "free@${domain}.test",
            isExternal = true
        )
        quark.userCreate(testUser, Quark.CreateAddress.WithKey())

        // WHEN
        AddAccountRobot
            .goToLogin()
            .fillUsername(testUser.email)
            .fillPassword(testUser.password)
            .login()

        ChooseInternalAddressRobot
            .apply {
                continueButtonIsEnabled()
            }
            .cancel()

        // THEN
        verifyLoggedOut()
    }
}
