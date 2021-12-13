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

import ProtonTestsExtension.Companion.setupProtonTestsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException

abstract class ProtonTestsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.setupTests()
    }
}

/**
 * Setup Tests for whole Project.
 * It will create a Gradle Task called "allTest" that will invoke "test" for jvm modules and "testDebugUnitTest" for
 * Android ones
 *
 * @param filter filter [Project.subprojects] to attach Publishing to
 *
 *
 * @author Davide Farella
 */
fun Project.setupTests(filter: (Project) -> Boolean = { true }) {

    // Configure sub-projects
    for (sub in subprojects.filter(filter)) {
        val options = sub.setupProtonTestsExtension()
        sub.afterEvaluate {
            tasks.register("allTest") {
                if (isAndroid) {
                    val flavorOption = (options.unitTestFlavor ?: "").capitalize()
                    val androidTestTaskName = "test${flavorOption}DebugUnitTest"
                    val androidTestTask = try {
                        tasks.named(androidTestTaskName)
                    } catch (unknownTaskException: UnknownTaskException) {
                        val message = "No android unit test task \'$androidTestTaskName\' found for project \'${sub.name}\'." +
                            "If you are using flavor you can set the flavor to use with \'protonTestsOptions.unitTestFlavor\'."
                        throw IllegalStateException(message, unknownTaskException)
                    }
                    dependsOn(androidTestTask)
                } else if (isJvm) dependsOn(tasks.getByName("test"))
            }
        }
    }

}

val Project.isJvm get() =
    plugins.hasPlugin("java-library")

val Project.isAndroid get() =
    plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")
