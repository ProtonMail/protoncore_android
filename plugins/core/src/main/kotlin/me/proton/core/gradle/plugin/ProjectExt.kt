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

import me.proton.core.gradle.convention.kotlin.KotlinConventionSettings
import me.proton.core.gradle.convention.android.AndroidConvention
import me.proton.core.gradle.convention.android.AndroidConventionSettings
import me.proton.core.gradle.convention.android.ComposeUiConvention
import me.proton.core.gradle.convention.dagger.DaggerConvention
import me.proton.core.gradle.convention.dagger.DaggerConventionSettings
import me.proton.core.gradle.convention.java.JavaConvention
import me.proton.core.gradle.convention.kotlin.KotlinConvention
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

internal inline fun <reified E : Any> Project.createProtonExt(name: String = "protonBuild"): E =
    extensions.create(name)

internal fun Project.applyAndroidConvention(settings: AndroidConventionSettings) =
    AndroidConvention().apply(this, settings)

internal fun Project.applyComposeUiConvention() =
    ComposeUiConvention().apply(this, Unit)

internal fun Project.applyJavaConvention() =
    JavaConvention().apply(this, Unit)

internal fun Project.applyKotlinConvention(settings: KotlinConventionSettings) =
    KotlinConvention().apply(this, settings)

internal fun Project.applyDaggerConvention(settings: DaggerConventionSettings) =
    DaggerConvention().apply(this, settings)
