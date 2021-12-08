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

import PublishPluginExtension.Companion.setupPublishOptionExtension
import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.plugin.devel.PluginDeclaration
import org.gradle.plugins.signing.SigningExtension
import java.io.File

abstract class ProtonPublishPluginsPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val version = target.computeVersionNameFromBranchName("release/gradle-plugins")
        val group = "me.proton.gradle-plugins"
        target.setupParentPublishing(group, version)
        target.subprojects {
            setupChildPublishing(group, version)
        }
        target.setupTagReleaseTask("release/gradle-plugins/$version")
    }
}

private fun Project.setupParentPublishing(groupName: String, versionName: String) {
    group = groupName
    version = versionName
    apply<NexusPublishPlugin>()
    configure<NexusPublishExtension> {
        repositories {
            sonatype {
                nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
                snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
                project.properties["mavenCentralUsername"]?.let { username.set(it as String) }
                project.properties["mavenCentralPassword"]?.let { password.set(it as String) }
            }
        }
    }
}

private fun Project.setupChildPublishing(groupName: String, versionName: String) {
    group = groupName
    version = versionName
    val publishOption = setupPublishOptionExtension()
    afterEvaluate {
        if (publishOption.shouldBePublishedAsPlugin) {
            setupProtonPluginPublishingPlugin()
            checkGradlePluginForPublishing()
            val gradle = extensions.getByType<GradlePluginDevelopmentExtension>()
            gradle.plugins.forEach { pluginDeclaration ->
                println("Setup publishing for plugin id=\'${pluginDeclaration.id}\' version=\'$versionName\'")
            }
        } else {
            println("Ignoring publishing for plugin project $name")
        }
    }
}

private fun Project.setupProtonPluginPublishingPlugin() {
    apply(plugin = "maven-publish")

    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    extensions.getByType(PublishingExtension::class).publications.withType(MavenPublication::class).configureEach {
        pom {
            name.set(artifactId)
            description.set("Proton gradle plugins for Android")
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

    configure<PublishingExtension> {}
    apply(plugin = "signing")
    configure<SigningExtension> {
        if (!version.toString().contains("SNAPSHOT")) {
            val signingKey: String? = project.properties["signingInMemoryKey"] as String?
            val signingPassphrase: String? = project.properties["signingInMemoryKeyPassword"] as String?
            if (signingKey.isNullOrBlank() || signingPassphrase.isNullOrBlank()) {
                throw Error("Signing keys for release version $version are missing, " +
                    "ensure signingInMemoryKey and signingInMemoryKeyPassword are set")
            }
            useInMemoryPgpKeys(signingKey, signingPassphrase)
            val publishExtension = extensions.getByType(PublishingExtension::class)
            sign(publishExtension.publications)
        }
    }
}

private fun Project.checkGradlePluginForPublishing() {
    val gradle = extensions.getByType<GradlePluginDevelopmentExtension>()
    gradle.plugins.forEach { pluginDeclaration ->
        checkPluginDeclarationForPublishing(pluginDeclaration)
        ensurePluginIdDocumented(pluginDeclaration)
    }
}

private fun checkPluginDeclarationForPublishing(pluginDeclaration: PluginDeclaration) {
    try {
        check(pluginDeclaration.id.startsWith("me.proton.gradle-plugins"))
        check(!pluginDeclaration.displayName.isNullOrBlank())
        check(!pluginDeclaration.description.isNullOrBlank())
        check(!pluginDeclaration.implementationClass.isNullOrBlank())
    } catch (exception: IllegalStateException) {
        exception.printStackTrace()
        println("Ensure you have a valid gradlePlugin block, see " +
            "https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html#configure_the_plugin_publishing_plugin")
        throw exception
    }
}

private fun Project.ensurePluginIdDocumented(pluginDeclaration: PluginDeclaration) {
    val readmeFile = File(rootDir.parent, "README.md")
    val readmeText = readmeFile.readText()
    val isPluginDocumented = readmeText.contains(pluginDeclaration.id)
    check(isPluginDocumented) {
        "Plugin id ${pluginDeclaration.id} is missing from README.MD, please document it"
    }
}
