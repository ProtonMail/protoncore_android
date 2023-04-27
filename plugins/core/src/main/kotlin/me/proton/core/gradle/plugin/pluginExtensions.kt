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

package me.proton.core.gradle.plugin

import me.proton.core.gradle.AndroidDefaults
import me.proton.core.gradle.DaggerDefaults
import me.proton.core.gradle.JvmDefaults
import me.proton.core.gradle.KotlinDefaults
import me.proton.core.gradle.convention.dagger.DaggerConventionSettings
import me.proton.core.gradle.convention.kotlin.KotlinConventionSettings
import org.gradle.api.JavaVersion
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import javax.inject.Inject

public open class DaggerExtension @Inject constructor() : DaggerConventionSettings {
    override var workManagerHiltIntegration: Boolean = DaggerDefaults.workManagerHiltIntegration
}

public open class AndroidAppExtension @Inject constructor(objects: ObjectFactory) :
    KotlinConventionSettings {
    override var apiMode: Property<ExplicitApiMode> =
        objects.property<ExplicitApiMode>().convention(ExplicitApiMode.Disabled)
}

public open class AndroidLibraryExtension @Inject constructor(objects: ObjectFactory) :
    KotlinConventionSettings {
    override var apiMode: Property<ExplicitApiMode> =
        objects.property<ExplicitApiMode>().convention(KotlinDefaults.apiMode)
}

public open class AndroidTestExtension @Inject constructor(objects: ObjectFactory) :
    KotlinConventionSettings {
    override var apiMode: Property<ExplicitApiMode> =
        objects.property<ExplicitApiMode>().convention(ExplicitApiMode.Disabled)
}

public open class AndroidUiLibraryExtension @Inject constructor(objects: ObjectFactory) :
    KotlinConventionSettings {
    override var apiMode: Property<ExplicitApiMode> =
        objects.property<ExplicitApiMode>().convention(KotlinDefaults.apiMode)
}

public open class ComposeUiLibraryExtension @Inject constructor(objects: ObjectFactory) :
    KotlinConventionSettings {
    override var apiMode: Property<ExplicitApiMode> =
        objects.property<ExplicitApiMode>().convention(KotlinDefaults.apiMode)
}

public open class CommonConfigurationExtension @Inject constructor(objects: ObjectFactory) :
    KotlinConventionSettings {
    override var apiMode: Property<ExplicitApiMode> =
        objects.property<ExplicitApiMode>().convention(KotlinDefaults.apiMode)
    public var compileSdk: Property<Int> =
        objects.property<Int>().convention(AndroidDefaults.compileSdk)
    public var jvmTarget: Property<JavaVersion> =
        objects.property<JavaVersion>().convention(JvmDefaults.jvmTarget)
    public var minSdk: Property<Int> = objects.property<Int>().convention(AndroidDefaults.minSdk)
    public var ndkVersion: Property<String> =
        objects.property<String>().convention(AndroidDefaults.ndkVersion)
    public var targetSdk: Property<Int> =
        objects.property<Int>().convention(AndroidDefaults.targetSdk)
    public var testInstrumentationRunner: Property<String> =
        objects.property<String>().convention(AndroidDefaults.testInstrumentationRunner)
}

public open class KotlinLibraryExtension @Inject constructor(objects: ObjectFactory) :
    KotlinConventionSettings {
    override var apiMode: Property<ExplicitApiMode> =
        objects.property<ExplicitApiMode>().convention(KotlinDefaults.apiMode)
}
