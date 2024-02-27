package me.proton.core.accountrecovery.data

import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.domain.entity.UserId
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalProtonFeatureFlag::class)
class IsAccountRecoveryResetEnabledImplTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var featureFlagManager: FeatureFlagManager

    @MockK
    private lateinit var resources: Resources

    private val userId = UserId("userId")
    private val featureId = IsAccountRecoveryResetEnabledImpl.featureId

    private lateinit var tested: IsAccountRecoveryResetEnabledImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        every { featureFlagManager.getValue(any(), any()) } returns false
        tested = IsAccountRecoveryResetEnabledImpl(context, featureFlagManager)
    }

    @Test
    fun accountRecoveryEnabled() {
        every { resources.getBoolean(R.bool.core_feature_account_recovery_reset_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns true
        assertTrue(tested(userId))
    }

    @Test
    fun accountRecoveryDisabled() {
        every { resources.getBoolean(R.bool.core_feature_account_recovery_reset_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns false
        assertFalse(tested(userId))
    }

    @Test
    fun accountRecoveryDisabledRemoteDisabled() {
        every { resources.getBoolean(R.bool.core_feature_account_recovery_reset_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns false
        assertFalse(tested(userId))
    }

    @Test
    fun accountRecoveryDisabledLocalDisabled() {
        every { resources.getBoolean(R.bool.core_feature_account_recovery_reset_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns true
        assertFalse(tested(userId))
    }
}
