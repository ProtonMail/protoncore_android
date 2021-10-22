/*
 * Copyright (c) 2020 Proton Technologies AG
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

import org.gradle.api.Project
import java.util.Locale

/** @return Name for archive ( e.g. `aar` ) for the given [Project] */
internal val Project.archiveName get() =
    "${fullQualifierName}_${pluginConfig?.version?.versionName}".noWhiteSpaces

/**
 * @return full human readable name of the receiver [Project]
 * Example: 'Test Kotlin'
 */
@OptIn(ExperimentalStdlibApi::class)
internal val Project.humanReadableName: String get() = name.split("-")
    .joinToString(" ") { it.capitalize(Locale.US) }

/**
 * @return full qualifier name of the receiver [Project]
 * Example: 'ProtonCore-test-kotlin'
 */
internal val Project.fullQualifierName: String get() = "${rootProject.name}-$name"

// region utils
private val String.noWhiteSpaces get() = replace(" ", "")
// endregion
