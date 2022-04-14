/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.conventionalcommits.command

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import me.proton.core.conventionalcommits.ConventionalCommit
import me.proton.core.conventionalcommits.usecase.GetCommits
import me.proton.core.conventionalcommits.usecase.GetConventionalCommits
import me.proton.core.conventionalcommits.usecase.GetLatestTag
import me.proton.core.conventionalcommits.usecase.GetVersionTags
import org.eclipse.jgit.revwalk.RevCommit
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

private const val BreakingChangeLogPrefix = "BREAKING CHANGE: "
private const val ShortCommitHashLength = 8

class ChangelogCommand : CliktCommand() {
    private val nextVersion by option().convert { it.trim() }.required()
    private val output by option().file(canBeDir = false, mustBeReadable = true, mustBeWritable = true)
    private val repoDir by repoDirOption().required()
    private val skipTypes by option().multiple(default = defaultChangelogSkipTypes)
    private val versionPrefix by versionPrefixOption()

    override fun run() {
        val partialChangelog = generatePartialChangelog()
        val file = output
        if (file == null) {
            echo(partialChangelog)
        } else {
            updateChangelogFile(file, partialChangelog)
        }
    }

    /** Generates a partial changelog, that covers changes starting from the latest existing release. */
    private fun generatePartialChangelog(): String {
        val repo = buildRepository(repoDir)
        val getVersionTags = GetVersionTags(repo, versionPrefix)
        val getCommits = GetCommits(repo)
        val latestTag = GetLatestTag(getVersionTags).invoke()
        val getConventionalCommits = GetConventionalCommits(getCommits)
        val changes = getConventionalCommits.invoke(since = latestTag?.objectId).filter { (_, commit) ->
            commit.type !in skipTypes && !commit.breaking
        }
        return renderPartialChangelog(changes, nextVersion)
    }

    /** Renders a partial changelog, covering the [version] and its [changes].
     * Sample output:
     *
     * ## [1.2.3] - 2022-01-31
     *
     * ### Bug Fixes
     *
     * - auth: fixed login process
     */
    private fun renderPartialChangelog(changes: List<Pair<RevCommit, ConventionalCommit>>, version: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        val currentDate = dateFormat.format(Date())

        return buildString {
            append("## [$version] - $currentDate")
            append('\n')

            changes
                .groupBy { it.second.type }
                .toList()
                .sortedBy { it.first } // sort by commit type (feat, fix etc.)
                .forEach { (type, commits) ->
                    append('\n')
                    append("### ")
                    append(getReadableCommitType(type))
                    append('\n')

                    commits
                        .sortedBy { it.first.commitTime }
                        .forEach { (revCommit, convCommit) ->
                            append('\n')
                            val shortCommitHash = revCommit.id.name.substring(0 until ShortCommitHashLength)
                            append(renderCommit(convCommit, shortCommitHash))
                        }
                }

            append('\n')
        }
    }

    private fun getReadableCommitType(conventionalType: String): String {
        return when (conventionalType.lowercase()) {
            "chore" -> "Chores"
            "fix" -> "Bug Fixes"
            "feat" -> "Features"
            "perf" -> "Performance Improvements"
            else -> conventionalType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    /** Renders a single conventional commit as a changelog entry.
     * Visible for testing.
     */
    internal fun renderCommit(convCommit: ConventionalCommit, shortCommitHash: String): String = buildString {
        append("- ")
        if (convCommit.breaking) {
            append(BreakingChangeLogPrefix)
        }

        if (convCommit.scope.isNotBlank()) {
            append(convCommit.scope)
            append(": ")
        }

        append(convCommit.description)

        append(" [")
        append(shortCommitHash)
        append("]")

        if (convCommit.body.isNotBlank()) {
            append("\n\n  ")
            val bodyWithIndentation = convCommit.body.lines().joinToString("\n  ")
            append(bodyWithIndentation)
        }

        if (convCommit.footers.isNotEmpty()) {
            append('\n')
        }

        convCommit.footers.forEach { footer ->
            append("\n  ")
            append(footer.key)
            append(": ")

            val footerValueWithIndentation = footer.value.lines().joinToString("\n  ")
            append(footerValueWithIndentation)
        }
    }

    /** Updates an [existingChangelogFile] by inserting a [partialChangelog]. */
    private fun updateChangelogFile(existingChangelogFile: File, partialChangelog: String) {
        val currentChangelog = existingChangelogFile.readText()
        if (currentChangelog.contains("## [$nextVersion]")) {
            echo("Version '$nextVersion' already exists in ${existingChangelogFile.absolutePath}", err = true)
            exitProcess(1)
        }

        val newChangelog = renderNewChangelog(partialChangelog, currentChangelog)
        existingChangelogFile.writeText(newChangelog)
    }

    // Visible for testing
    internal fun renderNewChangelog(partialChangelog: String, currentChangelog: String): String {
        val versionHeader = "## ["
        val unreleasedHeader = "## [Unreleased]"

        val indexAfterUnreleased: Int? = currentChangelog.indexOf(unreleasedHeader, ignoreCase = true).let {
            if (it >= 0) it + unreleasedHeader.length else null
        }

        val indexBeforeMostRecentVersion: Int? =
            currentChangelog.indexOf(versionHeader, startIndex = indexAfterUnreleased ?: 0).let {
                if (it >= 0) it else null
            }

        val unreleasedChangelog = if (indexAfterUnreleased != null) {
            currentChangelog.substring(
                indexAfterUnreleased until (indexBeforeMostRecentVersion ?: currentChangelog.length)
            )
        } else ""

        return buildString {
            if (indexAfterUnreleased != null) {
                append(currentChangelog.substring(0 until indexAfterUnreleased).trim())
                append("\n\n")
            } else if (indexBeforeMostRecentVersion != null) {
                append(currentChangelog.substring(0 until indexBeforeMostRecentVersion).trim())
                append("\n\n")
            }

            append(partialChangelog.trim())

            if (unreleasedChangelog.isNotBlank()) {
                append("\n\n")
                append(unreleasedChangelog.trim())
            }

            if (indexBeforeMostRecentVersion != null) {
                append("\n\n")
                append(currentChangelog.substring(indexBeforeMostRecentVersion).trim())
            }

            append('\n')
        }
    }
}
