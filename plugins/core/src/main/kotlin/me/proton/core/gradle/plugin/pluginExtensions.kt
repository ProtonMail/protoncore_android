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

package me.proton.core.gradle.plugin

import me.proton.core.gradle.DaggerDefaults
import me.proton.core.gradle.KotlinDefaults
import me.proton.core.gradle.convention.dagger.DaggerConventionSettings
import me.proton.core.gradle.convention.kotlin.KotlinConventionSettings
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import javax.inject.Inject

public open class DaggerExtension @Inject constructor() : DaggerConventionSettings {
    override var workManagerHiltIntegration: Boolean = DaggerDefaults.workManagerHiltIntegration
}

public open class AndroidAppExtension @Inject constructor() : KotlinConventionSettings {
    override var apiMode: ExplicitApiMode = ExplicitApiMode.Disabled
}

public open class AndroidLibraryExtension @Inject constructor() : KotlinConventionSettings {
    override var apiMode: ExplicitApiMode = KotlinDefaults.apiMode
}

public open class AndroidTestExtension @Inject constructor() : KotlinConventionSettings {
    override var apiMode: ExplicitApiMode = ExplicitApiMode.Disabled
}

public open class AndroidUiLibraryExtension @Inject constructor() : KotlinConventionSettings {
    override var apiMode: ExplicitApiMode = KotlinDefaults.apiMode
}

public open class ComposeUiLibraryExtension @Inject constructor() : KotlinConventionSettings {
    override var apiMode: ExplicitApiMode = KotlinDefaults.apiMode
}

public open class KotlinLibraryExtension @Inject constructor() : KotlinConventionSettings {
    override var apiMode: ExplicitApiMode = KotlinDefaults.apiMode
}
