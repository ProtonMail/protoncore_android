package me.proton.core.util.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Setup Kotlin for whole Project.
 * It will setup Kotlin compile options to sub-projects
 *
 * @param extraCompilerArgs
 *
 *
 * @author Davide Farella
 */
fun Project.setupKotlin(vararg extraCompilerArgs: String) {

    // Configure sub-projects
    for (sub in subprojects) {

        // Options for Kotlin
        sub.tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs = freeCompilerArgs + extraCompilerArgs
            }
        }
    }
}
