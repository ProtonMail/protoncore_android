/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.mailsettings.domain.entity

import me.proton.core.domain.type.StringEnum

data class MobileSettings(
    val listToolbar: ActionsToolbarSetting?,
    val messageToolbar: ActionsToolbarSetting?,
    val conversationToolbar: ActionsToolbarSetting?
)

data class ActionsToolbarSetting(
    val isCustom: Boolean?,
    val actions: List<StringEnum<ToolbarAction>>?
)

enum class ToolbarAction(val value: String) {
    ReplyOrReplyAll("reply"),
    Forward("forward"),
    MarkAsReadOrUnread("toggle_read"),
    StarOrUnstar("toggle_star"),
    LabelAs("label"),
    MoveTo("move"),
    MoveToTrash("trash"),
    MoveToArchive("archive"),
    MoveToSpam("spam"),
    ViewMessageInLightMode("toggle_light"),
    Print("print"),
    ReportPhishing("report_phishing");

    companion object {

        val map = entries.associateBy { it.value }
        fun enumOf(value: String) = StringEnum(value, ToolbarAction.map[value])
    }
}
