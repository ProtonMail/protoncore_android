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
import me.proton.core.conventionalcommits.ConventionalCommitParser
import me.proton.core.conventionalcommits.usecase.GetCommits
import org.eclipse.jgit.lib.Repository
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
    private val verbose by verboseOption()
    private val verifyFromCommit by VerifyFromCommitOptions().cooccurring()

    override fun run() {
        val verifyFromCommit = verifyFromCommit
        val commitMessages = if (verifyFromCommit != null) {
            val repo = buildRepository(verifyFromCommit.repoDir)
            getCommitMessages(verifyFromCommit.fromCommitSha, repo)
        } else {
            emptyList()
        }

        val allMessages = message + commitMessages

        val predicate: ((String) -> Boolean) = { msg: String ->
            val commit = ConventionalCommitParser.parse(msg)
            val successfullyParsed = commit != null
            val isTypeAllowed = commit?.type in allowType
            if (verbose) {
                if (successfullyParsed) {
                    println("Commit $commit; type allowed: $isTypeAllowed")
                } else {
                    println("Could not parse message: <<$msg>>")
                }
            }
            successfullyParsed && isTypeAllowed
        }

        val isMatching = if (matchAny) {
            allMessages.any(predicate)
        } else { // match all
            allMessages.all(predicate)
        }

        if (!isMatching) {
            exitProcess(1)
        }
    }

    private fun getCommitMessages(fromCommitSha: String, repo: Repository): List<String> {
        val since = repo.resolve(fromCommitSha) ?: error("Could not resolve commit $fromCommitSha")
        return GetCommits(repo)
            .invoke(since = since)
            .map { it.fullMessage }
    }
}
