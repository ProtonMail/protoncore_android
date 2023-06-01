package me.proton.core.accountrecovery.domain

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.justRun
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test

class AccountRecoveryNotificationManagerTest {
    @MockK
    private lateinit var configureAccountRecoveryChannel: ConfigureAccountRecoveryChannel

    @MockK
    private lateinit var getAccountRecoveryChannelId: GetAccountRecoveryChannelId

    @MockK
    private lateinit var isAccountRecoveryEnabled: IsAccountRecoveryEnabled

    private lateinit var tested: AccountRecoveryNotificationManager

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = AccountRecoveryNotificationManager(
            configureAccountRecoveryChannel,
            getAccountRecoveryChannelId,
            isAccountRecoveryEnabled
        )
    }

    @Test
    fun noSetupIfDisabled() {
        // GIVEN
        every { isAccountRecoveryEnabled() } returns false

        // WHEN
        tested.setupNotificationChannel()

        // THEN
        verify(exactly = 0) { getAccountRecoveryChannelId() }
        verify(exactly = 0) { configureAccountRecoveryChannel(any()) }
    }

    @Test
    fun setupIfEnabled() {
        // GIVEN
        every { isAccountRecoveryEnabled() } returns true
        every { getAccountRecoveryChannelId() } returns "channel-id"
        justRun { configureAccountRecoveryChannel(any()) }

        // WHEN
        tested.setupNotificationChannel()

        // THEN
        verify(exactly = 1) { getAccountRecoveryChannelId() }
        verify(exactly = 1) { configureAccountRecoveryChannel("channel-id") }
    }
}
