/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and ProtonCore.
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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import java.io.ByteArrayOutputStream

/**
 * Registered tasks:
 * * `allTest` ( 'me.proton.tests' plugin )
 * * `detekt` ( 'me.proton.detekt' plugin )
 * * `multiModuleDetekt` ( 'me.proton.detekt' plugin )
 * * `publishAll` ( 'me.proton.publish-libraries' plugin )
 * * `dependencyUpdates`
 */
plugins {
    id("me.proton.core.root")
    id("me.proton.core.gradle-plugins.detekt")
    id("publish-core-libraries")
    id("me.proton.core.gradle-plugins.tests")
    id("me.proton.core.gradle-plugins.coverage-config")
    alias(libs.plugins.benManes.versions.gradle)
    alias(libs.plugins.kotlin.binaryCompatibilityValidator)
    alias(libs.plugins.kotlin.gradle)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.kotlinx.kover) apply false
}

buildscript {
    repositories.google()

    dependencies {
        classpath(libs.android.gradle)
        classpath(libs.dagger.hilt.android.gradle)
    }
}

repositories {
    mavenCentral()
}

protonCoverage {
    excludes.add {
        classes(
            "me.proton.core.accountmanager.data.db.AccountManagerDatabaseMigrations*",
            "*Database_Impl*",
            "*Database\$Companion*", // DB migrations
            "*LogTag"
        )
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
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

dependencyAnalysis {
    dependencies {
        bundle("androidx-datastore") { includeGroup("androidx.datastore") }
        bundle("androidx-room") {
            includeGroup("androidx.room")
            includeGroup("androidx.sqlite")
        }
        bundle("dagger") {
            includeGroup("com.google.dagger")
            includeGroup("javax.inject")
        }
        bundle("kotlinx-coroutines-core") { include("org.jetbrains.kotlinx:kotlinx-coroutines-core.*") }
        bundle("kotlinx-serialization-core") { include("org.jetbrains.kotlinx:kotlinx-serialization-core.*") }
        bundle("kotlinx-serialization-json") { include("org.jetbrains.kotlinx:kotlinx-serialization-json.*") }
        bundle("mockk") { includeGroup("io.mockk") }
        bundle("robolectric") { includeGroup("org.robolectric") }
        bundle("squareup-networking") {
            includeGroup("com.squareup.okhttp3")
            includeGroup("com.squareup.okio")
            includeGroup("com.squareup.retrofit2")
        }
        bundle("turbine") { includeGroup("app.cash.turbine") }
    }
    issues {
        all {
            ignoreKtx(true)
            onAny {
                exclude(
                    "com.google.dagger:dagger-compiler",
                    "com.google.dagger:hilt-android-compiler",

                    // Dependencies that are automatically added by plugins:
                    "androidx.hilt:hilt-common",
                    "androidx.hilt:hilt-work",
                    "org.jetbrains.kotlin:kotlin-android-extensions-runtime",
                    "org.jetbrains.kotlin:kotlin-parcelize-runtime",
                )
            }
        }
    }
}

// Don't evaluate binary for some projects (see https://github.com/Kotlin/binary-compatibility-validator).
rootProject.apiValidation {
    ignoredProjects.add(rootProject.name)
    ignoredProjects.add("proguard-rules")
}
// Ignore non-published projects.
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
