package util

import android.databinding.tool.ext.capitalizeUS
import org.gradle.api.Project

/** @return Name for archive ( e.g. `aar` ) for the given [Project] */
val Project.archiveName get() =
    "${fullQualifierName}_${libVersion?.versionName}".noWhiteSpaces

/**
 * @return full human readable name of the receiver [Project]
 * Example: 'Test Kotlin'
 */
val Project.humanReadableName: String get() = name.split("-")
    .joinToString(" ") { it.capitalizeUS() }

/**
 * @return full qualifier name of the receiver [Project]
 * Example: 'ProtonCore-test-kotlin'
 */
val Project.fullQualifierName: String get() = "${rootProject.name}-$name"

// region utils
private val String.noWhiteSpaces get() = replace(" ", "")
// endregion
