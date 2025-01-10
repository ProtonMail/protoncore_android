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

package me.proton.core.mailsettings.domain.entity

import me.proton.core.domain.entity.UserId
import me.proton.core.domain.type.IntEnum
import me.proton.core.domain.type.StringEnum

data class MailSettings(
    val userId: UserId,
    val displayName: String?,
    val signature: String?,
    val autoSaveContacts: Boolean?,
    val composerMode: IntEnum<ComposerMode>?,
    val messageButtons: IntEnum<MessageButtons>?,
    val showImages: IntEnum<ShowImage>?,
    val showMoved: IntEnum<ShowMoved>?,
    val viewMode: IntEnum<ViewMode>?,
    val viewLayout: IntEnum<ViewLayout>?,
    val swipeLeft: IntEnum<SwipeAction>?,
    val swipeRight: IntEnum<SwipeAction>?,
    val shortcuts: Boolean?,
    val pmSignature: IntEnum<PMSignature>?,
    val numMessagePerPage: Int?,
    val autoDeleteSpamAndTrashDays: Int?,
    val almostAllMail: IntEnum<AlmostAllMail>?,
    val draftMimeType: StringEnum<MimeType>?,
    val receiveMimeType: StringEnum<MimeType>?,
    val showMimeType: StringEnum<MimeType>?,
    val enableFolderColor: Boolean?,
    val inheritParentFolderColor: Boolean?,
    val rightToLeft: Boolean?,
    val attachPublicKey: Boolean?,
    val sign: Boolean?,
    val pgpScheme: IntEnum<PackageType>?,
    val promptPin: Boolean?,
    val stickyLabels: Boolean?,
    val confirmLink: Boolean?,
    val mobileSettings: MobileSettings?
)

enum class ComposerMode(val value: Int) {
    Normal(0),
    Maximized(1);

    companion object {

        val map = values().associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}

/**
 * Read/unread toolbar order.
 */
enum class MessageButtons(val value: Int) {

    ReadFirst(0),
    UnreadFirst(1);

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}

/**
 * Auto-load image behavior, remove content & embedded images.
 */
enum class ShowImage(val value: Int) {

    None(0),
    Remote(1),
    Embedded(2),
    Both(3);

    fun includesRemote() =
        this == Remote || this == Both

    fun includesEmbedded() =
        this == Embedded || this == Both

    fun toggleRemote() = when (this) {
        None -> Remote
        Remote -> None
        Embedded -> Both
        Both -> Embedded
    }

    fun toggleEmbedded() = when (this) {
        None -> Embedded
        Remote -> Both
        Embedded -> None
        Both -> Remote
    }

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}

/**
 * Behavior for keep messages in Sent/Drafts even if you moved them or not.
 */
enum class ShowMoved(val value: Int) {

    None(0),
    Drafts(1),
    Sent(2),
    Both(3);

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}

/**
 * Conversation grouping view mode.
 */
enum class ViewMode(val value: Int) {

    ConversationGrouping(0),
    NoConversationGrouping(1);

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}

/**
 * Display conversations and opened message in rows or columns.
 */
enum class ViewLayout(val value: Int) {

    Column(0),
    Row(1);

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}

/**
 * Swipe action for Message/Conversation.
 */
enum class SwipeAction(val value: Int) {

    None(-1),
    Trash(0),
    Spam(1),
    Star(2),
    Archive(3),
    MarkRead(4),
    LabelAs(5),
    MoveTo(6);

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}

enum class PMSignature(val value: Int) {
    Enabled(0),
    Disabled(1);

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}

enum class PackageType(val type: Int) {
    ProtonMail(1),
    EncryptedOutside(2),
    Cleartext(4),
    PgpInline(8),
    PgpMime(16),
    ClearMime(32);

    companion object {

        val map = entries.associateBy { it.type }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }

        fun enumFromScheme(scheme: String, encrypt: Boolean, sign: Boolean): PackageType? =
            if (scheme == "pgp-mime") {
                if (!encrypt && sign) {
                    ClearMime
                } else if (encrypt) {
                    PgpMime
                } else Cleartext
            } else if (scheme == "pgp-inline") {
                if (encrypt) {
                    PgpInline
                } else Cleartext
            } else null
    }
}

enum class MimeType(val value: String) {
    Mixed("multipart/mixed"),
    PlainText("text/plain"),
    Html("text/html");

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: String?) = value?.let { StringEnum(it, map[it]) }

        fun enumFromContentType(contentType: String?): MimeType? = map[contentType]
    }
}

/**
 * Prevents items in "Trash"/"Spam" from being displayed in the "All Mail" folder.
 */
enum class AlmostAllMail(val value: Int) {
    Disabled(0),
    Enabled(1);

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: Int?) = value?.let { IntEnum(it, map[it]) }
    }
}
