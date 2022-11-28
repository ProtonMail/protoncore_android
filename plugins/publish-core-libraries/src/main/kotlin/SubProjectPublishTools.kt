
import PublishOptionExtension.Companion.setupPublishOptionExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import com.vanniktech.maven.publish.MavenPublishPluginExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.jetbrains.dokka.gradle.DokkaPlugin
import java.io.File

/*
 * Copyright (c) 2021 Proton Technologies AG
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

internal fun Project.setupSubProjectPublishing(groupName: String, versionName: String) {
    val publishOption = setupPublishOptionExtension()
    afterEvaluate {
        if (publishOption.shouldBePublishedAsLib) {
            setupCoordinates(groupName, versionName)
            logger.info("Setup publishing for $group:$name:$versionName")
        } else {
            logger.info("Ignoring publishing for $name")
        }
    }
}

private fun Project.setupCoordinates(groupName: String, versionName: String, generateKdoc: Boolean = false) {
    group = groupName
    val artifactId = name
    version = versionName

    // Dokka disabled by default
    // Dokka is slow and consume a lot of resource for not much value in our case as the documentation is read in AS
    // from sources.
    if (generateKdoc) apply<DokkaPlugin>()

    apply<MavenPublishPlugin>()
    configure<MavenPublishPluginExtension> {
        // Only sign non snapshot release
        releaseSigningEnabled = !versionName.contains("SNAPSHOT")
    }
    configure<MavenPublishBaseExtension> {
        pom {
            name.set(artifactId)
            description.set("Proton Core libraries for Android")
            url.set("https://github.com/ProtonMail/protoncore_android")
            licenses {
                license {
                    name.set("GNU GENERAL PUBLIC LICENSE, Version 3.0")
                    url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                }
            }
            developers {
                developer {
                    name.set("Open Source Proton")
                    email.set("opensource@proton.me")
                    id.set(email)
                }
            }
            scm {
                url.set("https://gitlab.protontech.ch/proton/mobile/android/proton-libs")
                connection.set("git@gitlab.protontech.ch:proton/mobile/android/proton-libs.git")
                developerConnection.set("https://gitlab.protontech.ch/proton/mobile/android/proton-libs.git")
            }
        }
    }
    ensureReleaseCoordinateDocumented()
}

private fun Project.ensureReleaseCoordinateDocumented() {
    val readmeFile = File(rootDir, "README.md")
    val readmeText = readmeFile.readText()
    val projectCoordinates = "$group:$name"
    val isCoordinatesDocumented = readmeText.contains(projectCoordinates)
    check(isCoordinatesDocumented) {
        "Artifact coordinates $projectCoordinates are missing in README.MD, please document it"
    }
}
