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

rootProject.name = "Proton Core"


val (projects, modules) = rootDir.projectsAndModules()
val namedProjects = projects.map { it to it.replace("/", "-") }
val namedModules = modules.map { it to it.drop(1).replace(":", "-") }

println("Projects: ${namedProjects.sortedBy { it.first }.joinToString { "${it.first} as ${it.second}" } }")
println("Modules: ${namedModules.sortedBy { it.first }.joinToString { "${it.first} as ${it.second}" } }")

for ((p, n) in namedProjects) includeBuild(p) { name = n }
for (m in modules) include(m)

for (m in namedModules) project(m.first).name = m.second

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://plugins.gradle.org/m2/")
    }
}


fun File.projectsAndModules(): Pair<Set<String>, Set<String>> {
    val blacklist = setOf(
        ".git",
        ".gradle",
        ".idea",
        "buildSrc",
        "config",
        "build",
        "src"
    )

    fun File.childrenDirectories() = listFiles { _, name -> name !in blacklist }!!
        .filter { it.isDirectory }

    fun File.isProject() =
        File(this, "settings.gradle.kts").exists() || File(this, "settings.gradle").exists()

    fun File.isModule() = !isProject() &&
        File(this, "build.gradle.kts").exists() || File(this, "build.gradle").exists()


    val modules = mutableSetOf<String>()
    val projects = mutableSetOf<String>()

    fun File.find(name: String? = null, includeModules: Boolean = true): List<File> = childrenDirectories().flatMap {
        val newName = (name ?: "") + it.name
        when {
            it.isProject() -> {
                projects += newName
                it.find("$newName:", includeModules = false)
            }
            it.isModule() && includeModules -> {
                modules += ":$newName"
                it.find("$newName:")
            }
            else -> it.find("$newName:")
        }
    }

    find()

    // we need to replace here since some Projects have a Module as a parent folder
    val formattedProjects = projects.map { it.replace(":", "/") }.toSet()
    return formattedProjects to modules
}

enableFeaturePreview("VERSION_CATALOGS")
