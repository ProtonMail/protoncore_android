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

import org.gradle.api.artifacts.dsl.DependencyHandler
import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

// Network
val DependencyHandler.`miniDsn` get() = dependency("org.minidns", module = "minidns-hla") version `miniDsn version`
val DependencyHandler.`retrofit-scalars-converter` get() = squareup("retrofit2", "converter-scalars") version `retrofit version`
val DependencyHandler.`mockWebServer` get() = squareup("okhttp3", "mockwebserver") version `okHttp version`
val DependencyHandler.`trustKit` get() = dependency("com.datatheorem.android.trustkit", module = "trustkit") version `trustKit version`

// Other
val DependencyHandler.`apacheCommon-codec` get() = dependency("commons-codec", module = "commons-codec") version `apacheCommon-codec version`
val DependencyHandler.`okHttp-logging` get() = squareup("okhttp3", module = "logging-interceptor") version `okHttp version`
val DependencyHandler.`lint-core` get() = lint()
val DependencyHandler.`lint-api` get() = lint("api")
val DependencyHandler.`lint-checks` get() = lint("checks")
val DependencyHandler.`lint-tests` get() = lint("tests")

// region accessors
fun DependencyHandler.lint(moduleSuffix: String? = null, version: String = `android-tools version`) =
    android("tools.lint", "lint", moduleSuffix, version)
// endregion
