import android.content.Context
import android.content.res.Resources
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.auth.data.R
import me.proton.core.auth.data.usecase.IsSsoCustomTabEnabledImpl
import me.proton.core.auth.data.usecase.IsSsoEnabledImpl
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalProtonFeatureFlag::class)
class IsSsoCustomTabEnabledImplTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var featureFlagManager: FeatureFlagManager

    @MockK
    private lateinit var resources: Resources

    private val featureId = IsSsoCustomTabEnabledImpl.featureId

    private lateinit var tested: IsSsoCustomTabEnabledImpl

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.resources } returns resources
        every { featureFlagManager.getValue(any(), any()) } returns false
        tested = IsSsoCustomTabEnabledImpl(context, featureFlagManager)
    }

    @Test
    fun ssoCustomTabEnabled() {
        every { resources.getBoolean(R.bool.core_feature_auth_sso_custom_tab_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns false
        assertTrue(tested())
    }

    @Test
    fun ssoCustomTabDisabled() {
        every { resources.getBoolean(R.bool.core_feature_auth_sso_custom_tab_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns true
        assertFalse(tested())
    }

    @Test
    fun ssoCustomTabDisabledRemoteKillSwitchDisabled() {
        every { resources.getBoolean(R.bool.core_feature_auth_sso_custom_tab_enabled) } returns true
        every { featureFlagManager.getValue(any(), featureId) } returns true
        assertFalse(tested())
    }

    @Test
    fun ssoCustomTabDisabledLocalDisabled() {
        every { resources.getBoolean(R.bool.core_feature_auth_sso_custom_tab_enabled) } returns false
        every { featureFlagManager.getValue(any(), featureId) } returns false
        assertFalse(tested())
    }
}
