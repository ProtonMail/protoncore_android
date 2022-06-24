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
import me.proton.core.conventionalcommits.usecase.ProposeNextVersion
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

private const val breakingChangesHeader = "Breaking Changes"

class ChangelogCommand : CliktCommand() {
    private val minorTypes by minorTypesOption()
    private val nextVersion by option().convert { it.trim() }
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
        val changes = getConventionalCommits(since = latestTag?.objectId)
            .map { it.second }
            .filter { commit ->
                commit.type !in skipTypes || commit.breaking
            }
        val version = nextVersion?.takeIf { it.isNotBlank() } ?: ProposeNextVersion(
            getConventionalCommits,
            minorTypes,
            versionPrefix
        ).invoke(latestTag)
        val renderer = ChangelogRenderer()
        return renderer(changes, Date(), version)
    }

    /** Updates an [existingChangelogFile] by inserting a [partialChangelog]. */
    private fun updateChangelogFile(existingChangelogFile: File, partialChangelog: String) {
        val currentChangelog = existingChangelogFile.readText()
        if (currentChangelog.contains("## [$nextVersion]")) {
            echo("Version '$nextVersion' already exists in ${existingChangelogFile.absolutePath}", err = true)
            exitProcess(1)
        }

        val newChangelog = updateChangelog(partialChangelog, currentChangelog)
        existingChangelogFile.writeText(newChangelog)
    }

    /** Inserts [partialChangelog] into the [currentChangelog] in appropriate place. */
    internal fun updateChangelog(partialChangelog: String, currentChangelog: String): String {
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

internal class ChangelogRenderer {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

    /** Renders a partial changelog, covering the [version] and its [changes]. */
    operator fun invoke(changes: List<ConventionalCommit>, date: Date, version: String): String = buildString {
        append("## [$version] - ${dateFormat.format(date)}")

        val (breakingChanges, nonBreakingChanges) = changes.partition { it.breaking }

        if (breakingChanges.isNotEmpty()) {
            append("\n\n### $breakingChangesHeader")
            append(renderCommits(breakingChanges, headerLevel = null, bold = true))
        }

        append(renderCommits(nonBreakingChanges, headerLevel = 3, bold = false))
        append('\n')
    }

    private fun renderCommits(commits: List<ConventionalCommit>, headerLevel: Int?, bold: Boolean) = buildString {
        commits
            .groupBy { it.type }
            .toList()
            .sortedBy { it.first } // sort by commit type (feat, fix etc.)
            .forEach { (type, commits) ->
                append("\n\n")
                headerLevel?.let {
                    append("#".repeat(it))
                    append(' ')
                }
                if (bold) append("**")
                append(getReadableCommitType(type))
                if (bold) append("**")

                append('\n')
                append(renderCommitTypeGroup(commits))
            }
    }

    /** Renders commits with the same type. */
    private fun renderCommitTypeGroup(commits: List<ConventionalCommit>): String = buildString {
        commits
            .groupByScope()
            .toList()
            .sortedBy { it.first } // sort by commit scope
            .forEach { (scope, commits) ->
                val indentLevel = if (scope.isBlank()) 0U else 1U
                if (scope.isNotBlank()) {
                    append('\n')
                    append("- $scope:")
                }
                commits.forEach { convCommit ->
                    append('\n')
                    append(renderCommit(convCommit, indentLevel = indentLevel))
                }
            }
    }

    private fun List<ConventionalCommit>.groupByScope(): Map<String, List<ConventionalCommit>> {
        val map = mutableMapOf<String, MutableList<ConventionalCommit>>()
        forEach { commit ->
            val scopeNames = if (commit.scopes.isEmpty()) {
                listOf("")
            } else {
                commit.scopes
            }
            scopeNames.forEach { scope ->
                val list = map.getOrPut(scope) { mutableListOf() }
                list.add(commit)
            }
        }
        return map
    }

    /** Renders a single conventional commit as a changelog entry.
     * Visible for testing.
     */
    internal fun renderCommit(convCommit: ConventionalCommit, indentLevel: UInt = 0U): String = buildString {
        val rootIndentation = "  ".repeat(indentLevel.toInt())
        val indentation = "  ".repeat(indentLevel.toInt() + 1)

        append("$rootIndentation- ")
        append(convCommit.description)

        if (convCommit.body.isNotBlank()) {
            append("\n\n$indentation")
            val bodyWithIndentation = convCommit.body.lines().joinToString("\n$indentation")
            append(bodyWithIndentation)
        }

        if (convCommit.footers.isNotEmpty()) append('\n')
        convCommit.footers.forEach { footer ->
            append("\n$indentation")
            append(footer.key)
            append(": ")

            val footerValueWithIndentation = footer.value.lines().joinToString("\n$indentation")
            append(footerValueWithIndentation)
        }
    }

    private fun getReadableCommitType(conventionalType: String): String {
        return when (conventionalType.lowercase()) {
            "chore" -> "Chores"
            "fix" -> "Bug Fixes"
            "feat" -> "Features"
            "i18n" -> "Internationalization"
            "perf" -> "Performance Improvements"
            "refactor" -> "Refactoring"
            "revert" -> "Reverted Changes"
            "theme" -> "Theming"
            else -> conventionalType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }
}
