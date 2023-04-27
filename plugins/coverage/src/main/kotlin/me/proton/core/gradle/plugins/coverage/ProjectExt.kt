package me.proton.core.gradle.plugins.coverage

import org.gradle.api.Project

private val koverTaskNameRegex = Regex("(.*:)?kover.*")
private val tasksTaskNameRegex = Regex("(.*:)?tasks")

/** Optional optimization.
 * Avoid further configuration if we're not trying to run a `kover` task,
 * or display the project's tasks.
 * Example:
 * > ./gradlew koverHtmlReport # <- The kover plugin will be configured
 * > ./gradlew tasks # <- The kover plugin will be configured
 * > ./gradlew someOtherTask # <- The kover plugin will NOT be configured
 */
internal fun Project.shouldSkipPluginApplication(): Boolean =
    !gradle.startParameter.taskNames.any {
        it.matches(koverTaskNameRegex) || it.matches(tasksTaskNameRegex)
    }
