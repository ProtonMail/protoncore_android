
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
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

internal fun Project.computeVersionName(): String {
    val branchName = runCommand("git branch --show-current")
    val versionName = if (branchName.startsWith("release/")) {
        branchName.substringAfter("release/")
    } else {
        "${branchName.replace('/', '-')}-SNAPSHOT"
    }
    return versionName
}

internal fun Project.registerPublishNewReleaseTask(versionName: String): TaskProvider<Task> {
    return tasks.register("publishNewRelease") {
        doLast {
            println("Publish artifacts for $versionName done")
        }
    }
}

internal fun Project.registerNotifyNewReleaseTask(versionName: String) {
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
    } else {
        println("Generating release-note.txt for release $versionName")
    }
    val fullChangelog = File("CHANGELOG.md")
    val fullChangelogText = fullChangelog.readText()
    val releaseChangelogText = fullChangelogText
        .substringAfter("## [$versionName]", "No changelog :cry:")
        .substringBefore("##")
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
    val slackPayloadFile = File("release-note.txt")
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
