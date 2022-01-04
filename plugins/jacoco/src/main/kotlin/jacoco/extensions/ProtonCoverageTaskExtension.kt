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

package jacoco.extensions

import org.gradle.api.Project

open class ProtonCoverageTaskExtension {

    /** Can be used to manually disable coverage for this module. */
    var isEnabled: Boolean = true

    /** Customize which task should run to generate test reports. If null the default implementation will be used. */
    var dependsOnTask: String? = null

    /** Customize excluded files for coverage in this module. */
    var excludes: List<String> = emptyList()

    /** Customize the source dirs for this module. */
    var sourceDirs: List<String> = emptyList()

    /** Generate a XML report for this module or not based on the module's setup. */
    var generatesXmlReport: (Project.() -> Boolean)? = null

    /** Generate a HTML report for this module or not based on the module's setup. */
    var generatesHtmlReport: (Project.() -> Boolean)? = null

    companion object {
        fun Project.setupCoverageExtension(): ProtonCoverageTaskExtension =
            extensions.create("protonCoverageOptions", ProtonCoverageTaskExtension::class.java)
    }
}
