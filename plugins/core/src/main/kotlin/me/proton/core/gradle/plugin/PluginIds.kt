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

internal object PluginIds {
    const val androidApp = "com.android.application"
    const val androidLibrary = "com.android.library"
    const val androidTest = "com.android.test"
    const val hilt = "dagger.hilt.android.plugin"
    const val javaLibrary = "org.gradle.java-library"
    const val kapt = "org.jetbrains.kotlin.kapt"
    const val kotlinAndroid = "org.jetbrains.kotlin.android"
    const val kotlinJvm = "org.jetbrains.kotlin.jvm"
}
