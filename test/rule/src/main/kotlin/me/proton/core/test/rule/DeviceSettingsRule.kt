/*
 * Copyright (c) 2023 Proton AG.
 * This file is part of Proton Drive.
 *
 * Proton Drive is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Drive is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Drive.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.test.rule

import android.provider.Settings.Global.ANIMATOR_DURATION_SCALE
import android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED
import android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE
import android.provider.Settings.Global.WINDOW_ANIMATION_SCALE
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.ExternalResource

/**
 * A JUnit test rule that configures device developer settings before test execution
 * and resets them after the test finishes.
 *
 * This rule is useful for improving the visibility and consistency of test runs,
 * especially for UI instrumentation tests that are recorded or analyzed visually.
 *
 * ## What it does:
 * - Enables developer settings.
 * - Disables or adjusts animations for consistent test timing.
 * - Enables visual touch indicators (`show_touches`) and pointer location for easier
 * debugging and video review.
 *
 * @param animationScale `Window animation scale` (e.g., 0.0 for no animation).
 * @param transitionAnimationScale `Transition animation scale` (e.g., 0.0 for instant transitions).
 * @param animatorDurationScale `Animator duration scale` (e.g., 0.0 to skip animations).
 *
 * @see android.provider.Settings.Global
 * @see android.provider.Settings.System
 */
public class DeviceSettingsRule(
    private val animationScale: Float = 0.0F,
    private val transitionAnimationScale: Float = 0.0F,
    private val animatorDurationScale: Float = 0.0F
) : ExternalResource() {
    private val cmd = "settings put global"
    private val cmdSystem = "settings put system"

    override fun before() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.run {
            executeShellCommand("$cmd $DEVELOPMENT_SETTINGS_ENABLED 1")
            executeShellCommand("$cmd $WINDOW_ANIMATION_SCALE $animationScale")
            executeShellCommand("$cmd $TRANSITION_ANIMATION_SCALE $transitionAnimationScale")
            executeShellCommand("$cmd $ANIMATOR_DURATION_SCALE $animatorDurationScale")
            executeShellCommand("$cmdSystem show_touches 1")
            executeShellCommand("$cmdSystem pointer_location 1")
        }
    }

    override fun after() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.run {
            executeShellCommand("$cmd $WINDOW_ANIMATION_SCALE 1.0")
            executeShellCommand("$cmd $TRANSITION_ANIMATION_SCALE 1.0")
            executeShellCommand("$cmd $ANIMATOR_DURATION_SCALE 1.0")
            executeShellCommand("$cmdSystem show_touches 0")
            executeShellCommand("$cmdSystem pointer_location 0")
        }
    }
}