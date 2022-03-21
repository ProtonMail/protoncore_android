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

package me.proton.core.challenge.data.api

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.challenge.data.deviceFontSize
import me.proton.core.challenge.data.deviceStorage
import me.proton.core.challenge.data.isDeviceRooted
import me.proton.core.challenge.data.nightMode
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails

@Serializable
data class RecoveryFrame(
    @SerialName("appLang")
    override val appLanguage: String,
    @SerialName("timezone")
    override val timezone: String,
    @SerialName("deviceName")
    override val deviceName: String,
    @SerialName("uuid")
    override val uid: String,
    @SerialName("regionCode")
    override val regionCode: String,
    @SerialName("timezoneOffset")
    override val timezoneOffset: Int,
    @SerialName("isJailbreak")
    override val rooted: Boolean,
    @SerialName("preferredContentSize")
    override val fontSize: String,
    @SerialName("storageCapacity")
    override val storage: Double,
    @SerialName("isDarkmodeOn")
    override val darkMode: Boolean,
    @SerialName("v")
    override val version: String,
    @SerialName("timeRecovery")
    val timeOnField: List<Long>,
    @SerialName("clickRecovery")
    val clickOnField: Int,
    @SerialName("copyRecovery")
    val copyField: List<String>,
    @SerialName("pasteRecovery")
    val pasteField: List<String>,
    @SerialName("frame")
    val frame: FrameType,
    @SerialName("keydownRecovery")
    override val keyDownField: List<Char>
) : Frame {
    companion object {
        fun from(context: Context, frame: ChallengeFrameDetails?): RecoveryFrame? =
            if (frame == null) {
                null
            } else
                RecoveryFrame(
                    appLanguage = "en",
                    timezone = "timezone",
                    deviceName = "device-name",
                    uid = "device-uid",
                    regionCode = "device-region",
                    timezoneOffset = 1,
                    rooted = isDeviceRooted(),
                    fontSize = context.deviceFontSize().toString(),
                    storage = context.deviceStorage(), // todo
                    darkMode = context.nightMode(),
                    timeOnField = listOf(frame.focusTime), // todo
                    clickOnField = frame.clicks,
                    copyField = frame.copy,
                    pasteField = frame.paste,
                    version = CHALLENGE_VERSION,
                    frame = FrameType("recovery"),
                    keyDownField = frame.keys
                )
    }
}
