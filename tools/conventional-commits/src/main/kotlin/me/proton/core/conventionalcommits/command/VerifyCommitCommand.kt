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
import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.groups.cooccurring
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import me.proton.core.conventionalcommits.ConventionalCommit
import me.proton.core.conventionalcommits.ConventionalCommitParser
import me.proton.core.conventionalcommits.usecase.GetCommits
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import kotlin.system.exitProcess

private class VerifyFromCommitOptions : OptionGroup() {
    val fromCommitSha by option().required()
    val repoDir by repoDirOption().required()
}

/** Verify if a commit message matches [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) spec. */
class VerifyCommitCommand : CliktCommand() {
    private val allowType by option().multiple(default = defaultAllowedTypes)
    private val matchAny by option().flag(default = false)
    private val message by option().multiple()
    private val printErrors by option().flag("--no-print-errors", default = true)
    private val verifyFromCommit by VerifyFromCommitOptions().cooccurring()

    override fun run() {
        val verifyFromCommit = verifyFromCommit
        val commitMessages = if (verifyFromCommit != null) {
            val repo = buildRepository(verifyFromCommit.repoDir)
            getCommitMessages(verifyFromCommit.fromCommitSha, repo)
        } else {
            emptyList()
        }

        val validator = CommitValidator(allowedCommitTypes = allowType)
        val results = message.map { validator(it) } +
            commitMessages.map { validator(it.fullMessage, commitHash = it.name) }

        if (printErrors) {
            results
                .filter { it != VerificationResult.Success }
                .forEach { println(it) }
        }

        val isMatching = if (matchAny) {
            results.any { it == VerificationResult.Success }
        } else { // match all
            results.all { it == VerificationResult.Success }
        }

        if (!isMatching) {
            exitProcess(1)
        }
    }

    private fun getCommitMessages(fromCommitSha: String, repo: Repository): List<RevCommit> {
        val since = repo.resolve(fromCommitSha) ?: error("Could not resolve commit $fromCommitSha")
        return GetCommits(repo).invoke(since = since)
    }
}

internal class CommitValidator(private val allowedCommitTypes: List<String>) {
    operator fun invoke(fullMessage: String, commitHash: String? = null): VerificationResult {
        val commit = ConventionalCommitParser.parse(fullMessage)
        return when {
            commit == null -> VerificationResult.ParseError(commitHash, fullMessage)
            commit.type !in allowedCommitTypes -> VerificationResult.TypeNotAllowed(commit, commitHash)
            !commit.description.isValidSentence() -> VerificationResult.InvalidDescriptionSentence(commit, commitHash)
            else -> VerificationResult.Success
        }
    }

    private fun String.isValidSentence(): Boolean = first().isUpperCase() && last() == '.'
}

internal sealed class VerificationResult {
    object Success : VerificationResult()

    data class ParseError(val commitHash: String?, val originalMessage: String) : VerificationResult() {
        override fun toString(): String {
            val firstLine = originalMessage.trim().replace(Regex("\n.*"), "...")
            return "$commitHash: Not a Conventional Commit: «$firstLine»."
        }
    }

    data class TypeNotAllowed(val commit: ConventionalCommit, val commitHash: String?) : VerificationResult() {
        override fun toString(): String {
            return "${commitHash}: Type «${commit.type}» is not allowed."
        }
    }

    data class InvalidDescriptionSentence(val commit: ConventionalCommit, val commitHash: String?) :
        VerificationResult() {
        override fun toString(): String {
            return "${commitHash}: Not a sentence: «${commit.description}» (start with uppercase, end with a dot)."
        }
    }
}
