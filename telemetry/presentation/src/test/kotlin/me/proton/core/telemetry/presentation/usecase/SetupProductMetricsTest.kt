/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.telemetry.presentation.usecase

import android.app.Activity
import android.app.Application
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.declaringKotlinFile
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import me.proton.core.presentation.utils.OnUiComponentCreatedListener
import me.proton.core.presentation.utils.UiComponent
import me.proton.core.presentation.utils.launchOnUiComponentCreated
import me.proton.core.telemetry.domain.TelemetryManager
import me.proton.core.telemetry.presentation.ProductMetricsDelegate
import me.proton.core.telemetry.presentation.ProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.annotation.ScreenClosed
import me.proton.core.telemetry.presentation.annotation.ScreenDisplayed
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import me.proton.core.telemetry.presentation.annotation.ViewFocused
import me.proton.core.telemetry.presentation.measureOnScreenClosed
import me.proton.core.telemetry.presentation.measureOnScreenDisplayed
import me.proton.core.telemetry.presentation.measureOnViewClicked
import me.proton.core.telemetry.presentation.measureOnViewFocused
import org.junit.Rule
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SetupProductMetricsTest {
    @get:Rule
    internal val instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var application: Application

    @MockK
    private lateinit var telemetryManager: TelemetryManager

    private lateinit var onUiComponentCreatedSlot: CapturingSlot<OnUiComponentCreatedListener>
    private lateinit var tested: SetupProductMetrics

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Application::launchOnUiComponentCreated.declaringKotlinFile.qualifiedName!!)
        mockkStatic(::measureOnScreenDisplayed.declaringKotlinFile.qualifiedName!!)
        mockkStatic(::measureOnScreenClosed.declaringKotlinFile.qualifiedName!!)
        mockkStatic(::measureOnViewClicked.declaringKotlinFile.qualifiedName!!)
        mockkStatic(::measureOnViewFocused.declaringKotlinFile.qualifiedName!!)

        onUiComponentCreatedSlot = slot()
        every { any<Application>().launchOnUiComponentCreated(capture(onUiComponentCreatedSlot)) } returns mockk()
        every { measureOnScreenDisplayed(any(), any(), any(), any(), any()) } returns mockk()
        every { measureOnScreenClosed(any(), any(), any(), any(), any()) } returns mockk()
        every { measureOnViewClicked(any(), any(), any()) } returns mockk()
        every { measureOnViewFocused(any(), any(), any()) } returns mockk()

        tested = SetupProductMetrics(application, telemetryManager)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `ui component with no metrics`() {
        // WHEN
        tested()
        onUiComponentCreatedSlot.captured.invoke(
            mockk(),
            mockk(),
            mockk(),
            mockk<UiComponent.UiActivity> {
                every { value } returns mockk<ActivityWithNoMetrics>()
            }
        )

        // THEN
        verify(exactly = 0) { measureOnScreenDisplayed(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { measureOnScreenClosed(any(), any(), any(), any(), any()) }
        verify(exactly = 0) { measureOnViewClicked(any(), any(), any()) }
        verify(exactly = 0) { measureOnViewFocused(any(), any(), any()) }
    }

    @Test
    fun `ui component with annotations`() {
        // WHEN
        tested()

        // THEN
        verify { application.launchOnUiComponentCreated(any()) }

        // WHEN - simulate creating an activity
        val view = mockk<View>(relaxed = true)
        onUiComponentCreatedSlot.captured.invoke(
            mockk(),
            mockk(),
            mockk(),
            mockk<UiComponent.UiActivity> {
                every { value } returns mockk<ActivityWithAnnotations>()
                every { findViewById<View>(any()) } returns view
                every { getIdentifier(any()) } returns 1
            }
        )

        // THEN
        val delegateOwnerSlot = slot<ProductMetricsDelegateOwner>()
        verify {
            measureOnScreenDisplayed(
                productEvent = "screen_displayed",
                productDimensions = mapOf("screen_dimension_displayed" to "sdd"),
                delegateOwner = capture(delegateOwnerSlot),
                lifecycleOwner = any(),
                savedStateRegistryOwner = any()
            )
        }
        assertEquals(
            "annotation_group",
            delegateOwnerSlot.captured.productMetricsDelegate.productGroup
        )
        assertEquals(
            "annotation_flow",
            delegateOwnerSlot.captured.productMetricsDelegate.productFlow
        )
        assertEquals(
            mapOf("annotation_dimension" to "ad1"),
            delegateOwnerSlot.captured.productMetricsDelegate.productDimensions
        )

        verify {
            measureOnScreenClosed(
                productEvent = "screen_closed",
                productDimensions = mapOf("screen_dimension_closed" to "sdc"),
                delegateOwner = any(),
                lifecycleOwner = any(),
                onBackPressedDispatcherOwner = any()
            )
        }

        verify {
            view.setOnClickListener(any())
        }

        verify {
            view.onFocusChangeListener = any()
        }
    }

    @Test
    fun `ui component with delegate`() {
        // WHEN
        val delegate = TestProductMetricsDelegate(telemetryManager)
        tested()
        onUiComponentCreatedSlot.captured.invoke(
            mockk(),
            mockk(),
            mockk(),
            mockk<UiComponent.UiActivity> {
                every { value } returns mockk<ActivityWithDelegate> {
                    every { productMetricsDelegate } returns delegate
                }
                every { findViewById<View>(any()) } returns mockk(relaxed = true)
                every { getIdentifier(any()) } returns 1
            }
        )

        // THEN
        val delegateOwnerSlot = slot<ProductMetricsDelegateOwner>()
        verify {
            measureOnScreenDisplayed(
                productEvent = "screen_displayed",
                productDimensions = mapOf("screen_dimension_displayed" to "sdd"),
                delegateOwner = capture(delegateOwnerSlot),
                lifecycleOwner = any(),
                savedStateRegistryOwner = any()
            )
        }
        assertSame(delegate, delegateOwnerSlot.captured.productMetricsDelegate)

        verify {
            measureOnScreenClosed(
                productEvent = "screen_closed",
                productDimensions = mapOf("screen_dimension_closed" to "sdc"),
                delegateOwner = any(),
                lifecycleOwner = any(),
                onBackPressedDispatcherOwner = any()
            )
        }
    }

    @Test
    fun `ui component with annotations and delegate`() {
        // WHEN
        tested()

        // THEN
        val throwable = assertFailsWith<IllegalStateException> {
            onUiComponentCreatedSlot.captured.invoke(
                mockk(),
                mockk(),
                mockk(),
                mockk<UiComponent.UiActivity> {
                    every { value } returns mockk<ActivityWithAnnotationAndDelegate> {
                        every { productMetricsDelegate } returns mockk()
                    }
                    every { findViewById<View>(any()) } returns mockk(relaxed = true)
                    every { getIdentifier(any()) } returns 1
                }
            )
        }
        assertTrue(throwable.message!!.startsWith("Cannot use both"))
    }

    @Test
    fun `convert array to map`() {
        assertTrue(emptyArray<String>().toMap().isEmpty())
        assertFailsWith<IllegalArgumentException> { arrayOf("a").toMap() }
        assertEquals(mapOf("a" to "1"), arrayOf("a", "1").toMap())
        assertFailsWith<IllegalArgumentException> { arrayOf("a", "1", "b").toMap() }
        assertEquals(mapOf("a" to "1", "b" to "2"), arrayOf("a", "1", "b", "2").toMap())
    }
}

private class ActivityWithNoMetrics : Activity()

@ProductMetrics(
    group = "annotation_group",
    flow = "annotation_flow",
    dimensions = ["annotation_dimension", "ad1"]
)
@ScreenDisplayed("screen_displayed", ["screen_dimension_displayed", "sdd"])
@ScreenClosed("screen_closed", ["screen_dimension_closed", "sdc"])
@ViewClicked("view_clicked", ["view1"])
@ViewFocused("view_focused", ["view2"])
private class ActivityWithAnnotations : Activity()

@ScreenDisplayed("screen_displayed", ["screen_dimension_displayed", "sdd"])
@ScreenClosed("screen_closed", ["screen_dimension_closed", "sdc"])
@ViewClicked("view_clicked", ["view1"])
@ViewFocused("view_focused", ["view2"])
private abstract class ActivityWithDelegate : Activity(), ProductMetricsDelegateOwner

private class TestProductMetricsDelegate(
    override val telemetryManager: TelemetryManager
) : ProductMetricsDelegate {
    override val productGroup: String = "delegate_group"
    override val productFlow: String = "delegate_flow"
    override val productDimensions: Map<String, String> = mapOf("delegate_dimension" to "dd")
}

@ProductMetrics(
    group = "annotation_group",
    flow = "annotation_flow",
    dimensions = ["annotation_dimension", "value"]
)
private class ActivityWithAnnotationAndDelegate : Activity(), ProductMetricsDelegateOwner {
    override val productMetricsDelegate: ProductMetricsDelegate = mockk()
}
