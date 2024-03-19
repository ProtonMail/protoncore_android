/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.test.android

import androidx.annotation.StringRes
import me.proton.core.test.android.instrumented.matchers.ToastOverlayMatcher
import me.proton.test.fusion.FusionConfig
import me.proton.test.fusion.ui.espresso.wrappers.EspressoAssertions
import me.proton.test.fusion.ui.espresso.wrappers.EspressoMatchers
import kotlin.time.Duration
import me.proton.test.fusion.ui.espresso.builders.OnView as FusionOnView

fun <T : FusionOnView> EspressoMatchers<T>.withToastAwait(
    @StringRes text: Int,
    timeout: Duration = FusionConfig.Espresso.waitTimeout.get(),
    interval: Duration = FusionConfig.Espresso.watchInterval.get(),
    assertion: EspressoAssertions.() -> EspressoAssertions
) = withToast(text).await(timeout, interval, assertion)

fun <T : FusionOnView> EspressoMatchers<T>.withToast(
    @StringRes text: Int,
) = withText(text).withRootMatcher(ToastOverlayMatcher())
