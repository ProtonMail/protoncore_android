/*
 * Copyright (c) 2024 Proton Technologies AG
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

package me.proton.core.configuration.configurator.quark.entity

import kotlinx.serialization.SerialName
import me.proton.core.test.quark.v2.QuarkCommand
import me.proton.core.test.quark.v2.toEncodedArgs
import okhttp3.Response
import java.util.regex.Pattern

const val USER_LIST_COMMAND = "quark/raw::user:list"
const val FIXTURES_LOAD_COMMAND = "quark/raw::qa:fixtures:load"
const val DOCTRINE_FIXTURES_LOAD_COMMAND = "quark/raw::doctrine:fixtures:load"

public fun QuarkCommand.createUserWithFixturesLoad(scenario: String): Response =
    route(FIXTURES_LOAD_COMMAND)
        .args(
            listOf(
                "definition-paths[]" to "nexus://Mail/ios/ios.$scenario",
                "--source[]" to "nexus:nexus:https://nexus.protontech.ch?repository=TestData",
                "--output-format" to "json"
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }


fun QuarkCommand.getAllUsers(): List<User> {
    val response = route(USER_LIST_COMMAND)
        .args(arrayOf())
        .build()
        .let {
            client.executeQuarkRequest(it)
        }
    val htmlResponse = response.body?.string() ?: throw IllegalStateException("Failed users fetching")

    val regexPattern = "^\\|\\s*(\\d+)\\s*\\|\\s*([^|]+?)\\s*\\|"
    val regex = Pattern.compile(regexPattern, Pattern.MULTILINE)
    val matcher = regex.matcher(htmlResponse)

    val users = mutableListOf<User>()

    while (matcher.find()) {
        val idString = matcher.group(1)
        val nameString = matcher.group(2)!!.trim()

        val id = idString?.toIntOrNull()
        if (id != null && nameString.isNotEmpty()) {
            users.add(User(id.toLong(), nameString))
        }
    }

    if (users.isEmpty()) {
        throw IllegalStateException("Failed users fetching")
    }

    return users
}


public fun QuarkCommand.doctrineFixturesLoad(scenario: String): Response =
    route(DOCTRINE_FIXTURES_LOAD_COMMAND)
        .args(
            listOf(
                "--append" to "1",
                "--group[]" to scenario
            ).toEncodedArgs()
        )
        .build()
        .let {
            client.executeQuarkRequest(it)
        }


data class User(
    val id: Long,
    val name: String,
    val plan: String = "",
    val password: String? = null
) {
    companion object {
        fun from(doctrineUser: DoctrineUser): User {
            return User(id = doctrineUser.userId, name = doctrineUser.name, password = doctrineUser.password)
        }
    }
}

data class QuarkUserResponse(
    val users: List<QuarkUser>
)

data class QuarkUser(
    val ID: QuarkID,
    val name: String,
    val password: String
)

data class QuarkID(
    val raw: Long
)

data class DoctrineUser(
    @SerialName("ID") val userId: Long,
    @SerialName("Name") val name: String,
    @SerialName("Password") val password: String,
    @SerialName("Status") val status: String,
    @SerialName("Recovery") val recovery: String,
    @SerialName("RecoveryPhone") val recoveryPhone: String?,
    @SerialName("AuthVersion") val authVersion: String,
    @SerialName("Email") val email: String,
    @SerialName("AddressID") val addressID: String?,
    @SerialName("AddressID (decrypt)") val decryptedAddressId: String?,
    @SerialName("KeySalt") val keySalt: String?,
    @SerialName("KeyFingerprint") val keyFingerprint: String?,
    @SerialName("MailboxPassword") val mailboxPassword: String?,
    @SerialName("ID (decrypt)") val decryptedUserId: String
)

data class MailScenario(
    val name: String,
    val description: String
)

object MailScenarios {
    val scenarios: List<MailScenario> = listOf(
        MailScenario("qa-mail-web-001", "1 message with remote content in Inbox"),
        MailScenario("qa-mail-web-002", "1 message with rich text in Inbox"),
        MailScenario("qa-mail-web-003", "1 message with empty body in Inbox"),
        MailScenario("qa-mail-web-004", "1 message with BCC in Sent"),
        MailScenario("qa-mail-web-005", "1 message with Unsubscribe in Inbox"),
        MailScenario("qa-mail-web-006", "3 messages in Inbox"),
        MailScenario("qa-mail-web-007", "1 messages with remote content and 1 message with tracked content in Inbox"),
        MailScenario("qa-mail-web-008", "3 messages with remote content in Sent"),
        MailScenario("qa-mail-web-009", "1 message with rich text in Archive"),
        MailScenario("qa-mail-web-010", "3 messages with remote content in Inbox"),
        MailScenario("qa-mail-web-011", "3 messages in Sent"),
        MailScenario("qa-mail-web-012", "2 conversations with remote content in Inbox"),
        MailScenario("qa-mail-web-013", "1 message with rich text in Archive"),
        MailScenario("qa-mail-web-014", "2 messages with remote content and 1 message with tracked content in Inbox"),
        MailScenario("qa-mail-web-015", "1 message with rich text in Trash"),
        MailScenario("qa-mail-web-016", "100 messages in Scheduled"),
        MailScenario("qa-mail-web-017", "7 messages in Archive"),
        MailScenario("qa-mail-web-018", "100 messages in Archive"),
        MailScenario("qa-mail-web-019", "1 message with rich text in Spam"),
        MailScenario("qa-mail-web-020", "1 message with rich text in Starred"),
        MailScenario("qa-mail-web-021", "1 message with Unsubscribe in Inbox"),
        MailScenario("qa-mail-web-022", "1 message with BCC in Inbox"),
        MailScenario("auto.reply", "auto.reply"),
        MailScenario("custom.swipe", "custom.swipe"),
        MailScenario("many.messages", "many.messages"),
        MailScenario("onepass.mailpro2022", "onepass.mailpro2022"),
        MailScenario("pgpinline", "pgpinline"),
        MailScenario("pgpinline.drafts", "pgpinline.drafts"),
        MailScenario("pgpinline.untrusted", "pgpinline.untrusted"),
        MailScenario("pgpmime", "pgpmime"),
        MailScenario("pgpmime.untrusted", "pgpmime.untrusted"),
        MailScenario("revoke.session", "revoke.session"),
        MailScenario("trash.multiple.messages", "trash.multiple.messages"),
        MailScenario("trash.one.message", "trash.one.message")
    )
}