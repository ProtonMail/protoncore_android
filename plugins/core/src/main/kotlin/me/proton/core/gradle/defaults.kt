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

package me.proton.core.gradle

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode

internal object AndroidDefaults {
    const val compileSdk = 31
    const val minSdk = 23
    const val ndkVersion = "21.3.6528147"
    const val targetSdk = 31
    const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
}

internal object DaggerDefaults {
    const val workManagerHiltIntegration = false
}

internal object JvmDefaults {
    val jvmTarget = JavaVersion.VERSION_1_8
}

internal object KotlinDefaults {
    val apiMode = ExplicitApiMode.Strict
}
