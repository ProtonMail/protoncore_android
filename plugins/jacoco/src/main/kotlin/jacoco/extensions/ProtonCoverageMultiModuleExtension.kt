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

/**
 * Allows customization of the coverage plugin at the root project's scope.
 */
open class ProtonCoverageMultiModuleExtension {

    /** Whether we should run test tasks before generating the coverage results or not. Defaults to true. */
    var runTestTasksBefore: Boolean = true

    /** Generate the merged XML report based on the current project's setup. */
    var generatesMergedXmlReport: Project.() -> Boolean = { true }

    /** Generate the merged HTML report based on the current project's setup. */
    var generatesMergedHtmlReport: Project.() -> Boolean = { true }

    /** Path to place the merged XML report based on the current project's setup. Should include file and extension. */
    var mergedXmlReportPath: (Project.() -> String)? = null

    /** Path to place the merged HTML report based on the current project's setup. Should be a directory. */
    var mergedHtmlReportPath: (Project.() -> String)? = null

    /** Path to place the Cobertura report based on the current project's setup. Should include file and extension. */
    var coberturaReportPath: (Project.() -> String)? = null

    /** Generate XML reports for each submodule based on that submodule's setup. */
    var generatesSubModuleXmlReports: Project.() -> Boolean = { true }

    /** Generate HTML reports for each submodule based on that submodule's setup. */
    var generatesSubModuleHtmlReports: Project.() -> Boolean = { true }

    /** Patterns to match files to be excluded from every submodule. */
    var sharedExcludes: List<String> = emptyList()

    companion object {
        fun Project.setupCoverageMultiModuleExtension(): ProtonCoverageMultiModuleExtension =
            extensions.create("protonCoverageMultiModuleOptions", ProtonCoverageMultiModuleExtension::class.java)
    }
}
