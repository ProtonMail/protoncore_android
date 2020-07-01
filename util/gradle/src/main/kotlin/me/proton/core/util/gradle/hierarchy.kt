package me.proton.core.util.gradle

import org.gradle.api.Project

/**
 * @return [List] of [Project] where the first item is the Root Project and the last on is the receiver one
 * @author Davide Farella
 */
@OptIn(ExperimentalStdlibApi::class)
fun Project.hierarchy() = buildList {
    var current: Project? = this@hierarchy
    while (current != null) {
        add(0, current)
        current = current.parent
    }
}
