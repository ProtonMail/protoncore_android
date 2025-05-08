package me.proton.core.passvalidator.data.feature

import android.content.Context
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.featureflag.domain.ExperimentalProtonFeatureFlag
import me.proton.core.featureflag.domain.FeatureFlagManager
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalProtonFeatureFlag::class)
class IsPasswordPolicyEnabledTest {
    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var featureFlagManager: FeatureFlagManager

    private lateinit var tested: IsPasswordPolicyEnabled

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
        tested = IsPasswordPolicyEnabled(context, featureFlagManager)
    }

    @Test
    fun `returns false if local flag is disabled`() {
        every { context.resources.getBoolean(any()) } returns false

        val result = tested(null)

        assertFalse(result)
    }

    @Test
    fun `returns false if remote kill switch is enabled`() {
        every { context.resources.getBoolean(any()) } returns true
        every { featureFlagManager.getValue(any(), any()) } returns true

        val result = tested(null)

        assertFalse(result)
    }

    @Test
    fun `returns true if local flag is enabled and kill switch disabled`() {
        every { context.resources.getBoolean(any()) } returns true
        every { featureFlagManager.getValue(any(), any()) } returns false

        val result = tested(null)

        assertTrue(result)
    }
}
