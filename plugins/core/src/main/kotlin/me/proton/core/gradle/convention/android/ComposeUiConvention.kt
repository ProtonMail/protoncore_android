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

@file:Suppress("UnstableApiUsage")

package me.proton.core.gradle.convention.android

import `compose version`
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import me.proton.core.gradle.convention.BuildConvention
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType

internal class ComposeUiConvention : BuildConvention<Unit> {
    override fun apply(target: Project, settings: Unit) {
        target.extensions.findByType<LibraryExtension>()?.applyConvention()
        target.extensions.findByType<ApplicationExtension>()?.applyConvention()
    }
}

private fun <T> T.applyConvention() where T : CommonExtension<*, *, *, *, *> {
    buildFeatures.compose = true

    composeOptions {
        kotlinCompilerExtensionVersion = `compose version`
    }
}
