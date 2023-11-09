import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.auth.data.R
import me.proton.core.auth.data.usecase.IsSsoEnabledImpl
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalProtonFeatureFlag::class)
class IsSsoEnabledImplTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var featureFlagManager: FeatureFlagManager

    @MockK
    private lateinit var resources: Resources

    private val featureId = IsSsoEnabledImpl.featureId

    private lateinit var tested: IsSsoEnabledImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        every { featureFlagManager.getValue(any(), any()) } returns false
        tested = IsSsoEnabledImpl(context, featureFlagManager)
    }

    @Test
    fun accountRecoveryEnabled() {
        every { resources.getBoolean(R.bool.core_feature_auth_sso_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns true
        assertTrue(tested())
    }

    @Test
    fun accountRecoveryDisabled() {
        every { resources.getBoolean(R.bool.core_feature_auth_sso_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns false
        assertFalse(tested())
    }

    @Test
    fun accountRecoveryDisabledRemoteDisabled() {
        every { resources.getBoolean(R.bool.core_feature_auth_sso_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns false
        assertFalse(tested())
    }

    @Test
    fun accountRecoveryDisabledLocalDisabled() {
        every { resources.getBoolean(R.bool.core_feature_auth_sso_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns true
        assertFalse(tested())
    }
}
