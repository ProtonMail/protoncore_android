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

@file:Suppress("unused")

package me.proton.core.util.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.kotlin.dsl.withType
import studio.forface.easygradle.dsl.*
import java.io.File

/**
 * Setup Dokka for whole Project.
 * It will:
 * * apply Dokka plugin to sub-projects
 * * configure Dokka Extension
 *
 * @param filter filter [Project.subprojects] to attach Dokka to
 *
 *
 * @author Davide Farella
 */
fun Project.setupDokka(filter: (Project) -> Boolean = { true }) {

    // Configure sub-projects
    for (sub in subprojects.filter(filter)) {
        sub.dokka {
            outputDirectory = File(rootDir, "docs").path +
                sub.hierarchy()
                    .drop(1)
                    .dropLast(1)
                    .joinToString(prefix = File.separator, separator = File.separator) { it.name }
        }

        // Disable JavaDoc
        sub.tasks.withType<Javadoc> { enabled = false }
    }
}
