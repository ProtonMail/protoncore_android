import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.io.ByteArrayOutputStream

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

/**
 * Registered tasks:
 * * `allTest` ( 'me.proton.tests' plugin )
 * * `detekt` ( 'me.proton.detekt' plugin )
 * * `multiModuleDetekt` ( 'me.proton.detekt' plugin )
 * * `publishAll` ( 'me.proton.publish-libraries' plugin )
 * * `dokka`
 * * `dependencyUpdates`
 */
plugins {
    id("me.proton.core.root")
    id("me.proton.core.gradle-plugins.detekt")
    id("publish-core-libraries")
    id("me.proton.core.gradle-plugins.tests")
    id("me.proton.core.gradle-plugins.jacoco")
    alias(libs.plugins.benManes.versions.gradle)
    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    alias(libs.plugins.kotlin.gradle)
    alias(libs.plugins.kotlin.serialization)
}

buildscript {
    repositories.google()

    dependencies {
        classpath(libs.dokka.gradle)
        classpath(libs.android.gradle)
        classpath(libs.dagger.hilt.android.gradle)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        // Disallow release candidates as upgradable versions from stable versions
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }

    checkForGradleUpdate = true
    outputFormatter = "json, html, plain"
    outputDir = "build/dependencyUpdates"
    reportfileName = "report"
}

tasks.register("generateChangelog", JavaExec::class.java) {
    dependsOn(gradle.includedBuild("tools").task(":conventional-commits:shadowJar"))

    classpath = files("tools/conventional-commits/build/libs/conventional-commits-all.jar")
    mainClass.set("me.proton.core.conventionalcommits.AppKt")

    args("changelog",
        "--repo-dir", projectDir.absolutePath,
        "--output", projectDir.resolve("CHANGELOG.md")
    )

    doFirst {
        // Make sure there are no uncommitted/unstaged changes for CHANGELOG.md:
        val output = ByteArrayOutputStream()
        exec {
            commandLine("git", "diff", "--name-only", "CHANGELOG.md")
            workingDir(projectDir)
            standardOutput = output
        }
        check(output.toString().isBlank()) { "Cannot update CHANGELOG.md file, because it has been modified." }
    }
}

protonCoverageMultiModuleOptions {
    generatesSubModuleHtmlReports = { !project.hasProperty("ci") }
    sharedExcludes = listOf(
        // androidTest code, shouldn't have coverage
        "**/me/proton/core/test/**",
        // Room Dao classes don't need testing
        "**/*Dao.class",
        // Migrations need to be done in instrumented tests which are currently not included in Jacoco reports
        "**/*MIGRATION*",
        // These components are tested using UI tests that are currently not included in Jacoco reports
        "**/*Activity.class",
        "**/*Activity$*",
        "**/*Fragment.class",
        "**/*Fragment$*",
    )
}

// Only evaluate binary api for to be published projects, see https://github.com/Kotlin/binary-compatibility-validator
subprojects {
    afterEvaluate {
        val publishOption = extensions.findByType(PublishOptionExtension::class.java)
        val shouldBePublishedAsLib = publishOption?.shouldBePublishedAsLib ?: false
        if (!shouldBePublishedAsLib) {
            rootProject.apiValidation {
                ignoredProjects.add(name)
            }
        }
    }
}
