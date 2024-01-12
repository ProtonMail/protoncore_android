package me.proton.core.featureflag.data

import android.content.Context
import android.content.res.Resources
import androidx.annotation.BoolRes
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.IsFeatureFlagEnabled
import me.proton.core.featureflag.domain.entity.FeatureId
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@BoolRes
private const val TEST_LOCAL_FLAG_ID: Int = 1
private val TEST_FEATURE_ID = FeatureId("testFeature")

@OptIn(ExperimentalProtonFeatureFlag::class)
class IsFeatureFlagEnabledImplTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var resources: Resources

    @MockK
    private lateinit var featureFlagManager: FeatureFlagManager

    private lateinit var tested: IsFeatureFlagEnabled

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        tested = IsTestFlagEnabledImpl(context, featureFlagManager)
    }

    @Test
    fun localEnabledRemoteEnabledReturnEnabled() {
        every { resources.getBoolean(TEST_LOCAL_FLAG_ID) } returns true
        every {
            featureFlagManager.getValue(any(), TEST_FEATURE_ID)
        } returns true

        assertTrue(tested(userId = null))
    }

    @Test
    fun localEnabledRemoteDisabledReturnDisabled() {
        every { resources.getBoolean(TEST_LOCAL_FLAG_ID) } returns true
        every {
            featureFlagManager.getValue(any(), TEST_FEATURE_ID)
        } returns false

        assertFalse(tested(userId = null))
    }

    @Test
    fun localDisabledRemoteEnabledReturnDisabled() {
        every { resources.getBoolean(TEST_LOCAL_FLAG_ID) } returns false
        every {
            featureFlagManager.getValue(any(), TEST_FEATURE_ID)
        } returns true

        assertFalse(tested(userId = null))
    }

    @Test
    fun localDisabledRemoteDisabledReturnDisabled() {
        every { resources.getBoolean(TEST_LOCAL_FLAG_ID) } returns false
        every {
            featureFlagManager.getValue(any(), TEST_FEATURE_ID)
        } returns false

        assertFalse(tested(userId = null))
    }

    private class IsTestFlagEnabledImpl(
        @ApplicationContext context: Context,
        featureFlagManager: FeatureFlagManager,
    ) : IsFeatureFlagEnabledImpl(
        context,
        featureFlagManager,
        TEST_FEATURE_ID,
        TEST_LOCAL_FLAG_ID
    )
}
