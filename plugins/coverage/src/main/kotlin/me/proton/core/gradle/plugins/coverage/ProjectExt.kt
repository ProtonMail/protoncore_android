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

package me.proton.core.gradle.plugins.coverage

import org.gradle.api.Project

private val globalLineCoverageTaskNameRegex = Regex("(.*:)?$globalLineCoverageTaskName")
private val koverTaskNameRegex = Regex("(.*:)?kover.*")
private val tasksTaskNameRegex = Regex("(.*:)?tasks")

/** Optional optimization.
 * Avoid further configuration if we're not trying to run a `kover` task,
 * or display the project's tasks.
 * Example:
 * > ./gradlew koverHtmlReport # <- The kover plugin will be configured
 * > ./gradlew tasks # <- The kover plugin will be configured
 * > ./gradlew someOtherTask # <- The kover plugin will NOT be configured
 */
internal fun Project.shouldSkipPluginApplication(): Boolean =
    !gradle.startParameter.taskNames.any {
        it.matches(koverTaskNameRegex) ||
                it.matches(tasksTaskNameRegex) ||
                it.matches(globalLineCoverageTaskNameRegex)
    }
