package me.proton.core.accountrecovery.data

import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsAccountRecoveryEnabledImplTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    private lateinit var tested: IsAccountRecoveryEnabledImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        tested = IsAccountRecoveryEnabledImpl(context)
    }

    @Test
    fun accountRecoveryEnabled() {
        every { resources.getBoolean(R.bool.core_feature_account_recovery_enabled) } returns true
        assertTrue(tested())
    }

    @Test
    fun accountRecoveryDisabled() {
        every { resources.getBoolean(R.bool.core_feature_account_recovery_enabled) } returns false
        assertFalse(tested())
    }
}
