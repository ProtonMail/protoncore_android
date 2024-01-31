package me.proton.core.auth.data.usecase

import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.test.runTest
import me.proton.core.auth.data.R
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.entity.Scope
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalProtonFeatureFlag::class)
class IsCredentialLessEnabledTest {

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var featureFlagManager: FeatureFlagManager

    @MockK
    private lateinit var resources: Resources

    private lateinit var tested: IsCredentialLessEnabledImpl

    private val featureId = IsCredentialLessEnabledImpl.featureId

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        every { featureFlagManager.getValue(any(), any()) } returns false
        coEvery { featureFlagManager.awaitNotEmptyScope(any(), Scope.Unleash) } just Runs
        tested = IsCredentialLessEnabledImpl(context, featureFlagManager)
    }

    @Test
    fun localFeatureEnabledRemoteNotDisabled() = runTest {
        every { resources.getBoolean(R.bool.core_feature_credential_less_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns false
        assertTrue(tested())
    }

    @Test
    fun localFeatureEnabledRemoteDisabled() = runTest {
        every { resources.getBoolean(R.bool.core_feature_credential_less_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns true
        assertFalse(tested())
    }

    @Test
    fun localFeatureDisabledRemoteDisabled() = runTest {
        every { resources.getBoolean(R.bool.core_feature_credential_less_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns true
        assertFalse(tested())
    }

    @Test
    fun localFeatureDisabledRemoteNotDisabled() = runTest {
        every { resources.getBoolean(R.bool.core_feature_credential_less_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns false
        assertFalse(tested())
    }
}
