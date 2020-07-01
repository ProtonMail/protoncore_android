import org.gradle.api.Project
import org.gradle.kotlin.dsl.kotlin
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

/*
 * Set of fields that will apply a plugin on a `PluginDependenciesSpec` scope
 * Usage example:
 * ```
 plugins {
    `kotlin-library`
    `kotlin-serialization`
 }
 * ```
 *
 * Author: Davide Farella
 */

val PluginDependenciesSpec.`kotlin-kapt` get() =                kotlin("kapt")
val PluginDependenciesSpec.`kotlin-serialization` get() =       kotlin("plugin.serialization")

val PluginDependenciesSpec.`kotlin-library`: Unit get() {
    plugin("java-library")
    plugin("kotlin")
}

val PluginDependenciesSpec.`android-application`: Unit get() {
    plugin("com.android.application")
    kotlin("android")
}

val PluginDependenciesSpec.`android-library`: Unit get() {
    plugin("com.android.library")
    kotlin("android")
}


/*
 * Set of fields that will return a String id of a plugin into a `Project` scope
 * Usage example:
 * `` apply(plugin = `detekt`) ``
 *
 * Author: Davide Farella
 */

val Project.`detekt id` get() = "io.gitlab.arturbosch.detekt"


private fun PluginDependenciesSpec.plugin(id: String): PluginDependencySpec = id(id)
