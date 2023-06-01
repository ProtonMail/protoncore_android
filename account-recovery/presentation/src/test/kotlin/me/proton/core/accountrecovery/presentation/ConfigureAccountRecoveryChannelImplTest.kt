package me.proton.core.accountrecovery.presentation

import android.app.NotificationManager
import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test

class ConfigureAccountRecoveryChannelImplTest {
    @MockK
    private lateinit var context: Context
    private lateinit var tested: ConfigureAccountRecoveryChannelImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = ConfigureAccountRecoveryChannelImpl(context)
    }

    @Test
    fun configureChannel() {
        val notificationManager = mockk<NotificationManager>()
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        every { context.getString(any()) } returns "Channel name"
        tested("channel-id")
    }
}
