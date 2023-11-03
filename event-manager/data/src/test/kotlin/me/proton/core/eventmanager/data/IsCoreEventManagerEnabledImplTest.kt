package me.proton.core.eventmanager.data

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
class IsCoreEventManagerEnabledImplTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var featureFlagManager: FeatureFlagManager

    private lateinit var tested: IsCoreEventManagerEnabledImpl

    private val userId = UserId("userId")

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        tested = IsCoreEventManagerEnabledImpl(context, featureFlagManager)
    }

    @Test
    fun localEnabledRemoteEnabledReturnEnabled() {
        every { resources.getBoolean(R.bool.core_feature_core_event_manager_enabled) } returns true
        every { featureFlagManager.getValue(any(), IsCoreEventManagerEnabledImpl.featureId) } returns false

        assertTrue(tested(userId))
    }

    @Test
    fun localEnabledRemoteDisabledReturnDisabled() {
        every { resources.getBoolean(R.bool.core_feature_core_event_manager_enabled) } returns true
        every { featureFlagManager.getValue(any(), IsCoreEventManagerEnabledImpl.featureId) } returns true

        assertFalse(tested(userId))
    }

    @Test
    fun localDisabledRemoteEnabledReturnDisabled() {
        every { resources.getBoolean(R.bool.core_feature_core_event_manager_enabled) } returns false
        every { featureFlagManager.getValue(any(), IsCoreEventManagerEnabledImpl.featureId) } returns false

        assertFalse(tested(userId))
    }

    @Test
    fun localDisabledRemoteDisabledReturnDisabled() {
        every { resources.getBoolean(R.bool.core_feature_core_event_manager_enabled) } returns false
        every { featureFlagManager.getValue(any(), IsCoreEventManagerEnabledImpl.featureId) } returns true

        assertFalse(tested(userId))
    }
}
