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

package me.proton.core.auth.data.api.request

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.challenge.data.appLanguage
import me.proton.core.challenge.data.deviceFontSize
import me.proton.core.challenge.data.deviceModelName
import me.proton.core.challenge.data.deviceRegion
import me.proton.core.challenge.data.deviceStorage
import me.proton.core.challenge.data.deviceTimezone
import me.proton.core.challenge.data.deviceTimezoneOffset
import me.proton.core.challenge.data.deviceUID
import me.proton.core.challenge.data.isDeviceRooted
import me.proton.core.challenge.data.nightMode
import me.proton.core.challenge.domain.CHALLENGE_VERSION
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails

@Serializable
sealed class AuthChallengeFrame {

    abstract val appLanguage: String
    abstract val timezone: String
    abstract val deviceName: String
    abstract val uid: String
    abstract val regionCode: String
    abstract val timezoneOffset: Int
    abstract val rooted: Boolean
    abstract val fontSize: String
    abstract val storage: Double
    abstract val darkMode: Boolean
    abstract val version: String
    abstract val keyDownField: List<String>


    @Serializable
    data class AuthChallengeUsernameFrame(
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
        @SerialName("timeUsername")
        val timeOnField: List<Int>,
        @SerialName("clickUsername")
        val clickOnField: Int,
        @SerialName("copyUsername")
        val copyField: List<String>,
        @SerialName("pasteUsername")
        val pasteField: List<String>,
        @SerialName("frame")
        val frame: ChallengeFrameType,
        @SerialName("keydownUsername")
        override val keyDownField: List<String>
    ) : AuthChallengeFrame() {
        companion object {
            fun from(context: Context, frame: ChallengeFrameDetails?): AuthChallengeUsernameFrame? =
                if (frame == null) {
                    null
                } else
                    AuthChallengeUsernameFrame(
                        appLanguage = appLanguage(),
                        timezone = deviceTimezone(),
                        deviceName = deviceModelName(),
                        uid = deviceUID(),
                        regionCode = context.deviceRegion(),
                        timezoneOffset = deviceTimezoneOffset(),
                        rooted = isDeviceRooted(),
                        fontSize = context.deviceFontSize().toString(),
                        storage = context.deviceStorage(),
                        darkMode = context.nightMode(),
                        timeOnField = frame.focusTime,
                        clickOnField = frame.clicks,
                        copyField = frame.copy,
                        pasteField = frame.paste,
                        version = CHALLENGE_VERSION,
                        frame = ChallengeFrameType(frame.challengeFrame),
                        keyDownField = frame.keys
                    )
        }
    }
}
