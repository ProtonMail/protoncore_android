/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

public inline val PluginDependenciesSpec.protonAndroidApp: PluginDependencySpec
    get() = id("me.proton.core.app.android")

public inline val PluginDependenciesSpec.protonAndroidLibrary: PluginDependencySpec
    get() = id("me.proton.core.library.android")

public inline val PluginDependenciesSpec.protonAndroidTest: PluginDependencySpec
    get() = id("me.proton.core.test.android")

public inline val PluginDependenciesSpec.protonAndroidUiLibrary: PluginDependencySpec
    get() = id("me.proton.core.library.android.ui")

public inline val PluginDependenciesSpec.protonComposeUiLibrary: PluginDependencySpec
    get() = id("me.proton.core.library.android.ui.compose")

public inline val PluginDependenciesSpec.protonDagger: PluginDependencySpec
    get() = id("me.proton.core.dagger")

public inline val PluginDependenciesSpec.protonKotlinLibrary: PluginDependencySpec
    get() = id("me.proton.core.library.kotlin")
