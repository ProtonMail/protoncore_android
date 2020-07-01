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
