
import io.github.gradlenexus.publishplugin.NexusPublishExtension
import io.github.gradlenexus.publishplugin.NexusPublishPlugin
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra
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

/**
 * Fully support unique staging repository creation for multi project release
 * See https://github.com/gradle-nexus/publish-plugin/ and
 * https://github.com/gradle-nexus/publish-plugin/#behind-the-scenes
 */
internal fun Project.setupPublishingTasks(groupName: String, versionName: String) {
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

internal fun Project.setupNotifyNewReleaseTask(versionName: String) {
    tasks.register("notifyNewRelease") {
        doLast {
            val releaseNote = generateReleaseNoteIfNeeded(versionName)
            releaseNote?.let {
                notifyRelease(releaseNote)
            }
        }
    }
}

private fun generateReleaseNoteIfNeeded(versionName: String): String? {
    if (versionName.contains("SNAPSHOT")) {
        println("Ignoring release note generation due to snapshot release")
        return null
    }
    println("Generating release note for release $versionName")
    val fullChangelog = File("CHANGELOG.md")
    val fullChangelogText = fullChangelog.readText()
    val releaseChangelogText = fullChangelogText
        .substringAfter("## [$versionName]", "No changelog :cry:")
        .substringBefore("## [")
    return StringBuilder()
        .appendLine("New Core release available `$versionName` :tada:")
        .appendLine(releaseChangelogText)
        .toString()
}

private fun Project.notifyRelease(releaseNote: String) {
    val extraHookName = "androidDevChannelWebhook"
    val hasHook = extra.has(extraHookName)
    if (!hasHook) {
        println("Warning: No slack webhook provided")
        return
    }
    val hook = extra[extraHookName] as String
    val slackPayloadFile = File("slack.tmp")
    slackPayloadFile.delete()
    slackPayloadFile.createNewFile()
    // https://api.slack.com/messaging/webhooks#advanced_message_formatting
    val slackPayloadText = SlackText(type = "mrkdwn", text = escapeMarkdownForSlack(releaseNote))
    slackPayloadFile.writeText(Json.encodeToString(slackPayloadText))
    val result = runCommand(
        command = "curl --show-error --fail",
        args = listOf(
            "--header", "Content-type: application/json",
            "--data-binary", "@${slackPayloadFile.path}",
            hook
        )
    )
    slackPayloadFile.delete()
    println(result)
}

@Serializable
data class SlackText(
    val type: String,
    val text: String
)

/**
 * https://api.slack.com/reference/surfaces/formatting#escaping
 */
private fun escapeMarkdownForSlack(releaseNote: String) = releaseNote
    .replace("&", "&amp;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")
