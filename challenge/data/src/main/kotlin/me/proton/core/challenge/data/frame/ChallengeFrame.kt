package me.proton.core.challenge.data.frame

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.challenge.domain.CHALLENGE_VERSION
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails
import me.proton.core.util.android.device.appLanguage
import me.proton.core.util.android.device.deviceFontSize
import me.proton.core.util.android.device.deviceInputMethods
import me.proton.core.util.android.device.deviceModelName
import me.proton.core.util.android.device.deviceRegion
import me.proton.core.util.android.device.deviceStorage
import me.proton.core.util.android.device.deviceTimezone
import me.proton.core.util.android.device.deviceTimezoneOffset
import me.proton.core.util.android.device.isDeviceRooted
import me.proton.core.util.android.device.nightMode

@Serializable
public sealed class ChallengeFrame {

    public abstract val version: String
    public abstract val appLanguage: String
    public abstract val timezone: String
    public abstract val deviceName: Long
    public abstract val regionCode: String
    public abstract val timezoneOffset: Int
    public abstract val rooted: Boolean
    public abstract val fontSize: String
    public abstract val storage: Double
    public abstract val darkMode: Boolean
    public abstract val keyboards: List<String>

    @Serializable
    public data class Device(
        @SerialName("v")
        public override val version: String,
        @SerialName("appLang")
        public override val appLanguage: String,
        @SerialName("timezone")
        public override val timezone: String,
        @SerialName("deviceName")
        public override val deviceName: Long,
        @SerialName("regionCode")
        public override val regionCode: String,
        @SerialName("timezoneOffset")
        public override val timezoneOffset: Int,
        @SerialName("isJailbreak")
        public override val rooted: Boolean,
        @SerialName("preferredContentSize")
        public override val fontSize: String,
        @SerialName("storageCapacity")
        public override val storage: Double,
        @SerialName("isDarkmodeOn")
        public override val darkMode: Boolean,
        @SerialName("keyboards")
        public override val keyboards: List<String>
    ) : ChallengeFrame() {
        public companion object {
            public suspend fun build(context: Context): Device = Device(
                version = CHALLENGE_VERSION,
                appLanguage = appLanguage(),
                timezone = deviceTimezone(),
                deviceName = deviceModelName(),
                regionCode = context.deviceRegion(),
                timezoneOffset = deviceTimezoneOffset(),
                rooted = isDeviceRooted(context),
                fontSize = context.deviceFontSize().toString(),
                storage = context.deviceStorage(),
                darkMode = context.nightMode(),
                keyboards = context.deviceInputMethods()
            )
        }
    }

    @Serializable
    public data class Recovery(
        @SerialName("v")
        public override val version: String,
        @SerialName("appLang")
        override val appLanguage: String,
        @SerialName("timezone")
        override val timezone: String,
        @SerialName("deviceName")
        override val deviceName: Long,
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
        @SerialName("keyboards")
        override val keyboards: List<String>,
        @SerialName("frame")
        public val frame: FrameType,
        @SerialName("timeRecovery")
        public val timeOnField: List<Int>,
        @SerialName("clickRecovery")
        public val clickOnField: Int,
        @SerialName("copyRecovery")
        public val copyField: List<String>,
        @SerialName("pasteRecovery")
        public val pasteField: List<String>,
        @SerialName("keydownRecovery")
        public val keyDownField: List<String>,
    ) : ChallengeFrame() {
        public companion object {
            public suspend fun from(context: Context, frame: ChallengeFrameDetails?): Recovery? {
                if (frame == null) return null
                return Recovery(
                    version = CHALLENGE_VERSION,
                    appLanguage = appLanguage(),
                    timezone = deviceTimezone(),
                    deviceName = deviceModelName(),
                    regionCode = context.deviceRegion(),
                    timezoneOffset = deviceTimezoneOffset(),
                    rooted = isDeviceRooted(context),
                    fontSize = context.deviceFontSize().toString(),
                    storage = context.deviceStorage(),
                    darkMode = context.nightMode(),
                    keyboards = context.deviceInputMethods(),
                    frame = FrameType(frame.challengeFrame),
                    timeOnField = frame.focusTime,
                    clickOnField = frame.clicks,
                    copyField = frame.copy,
                    pasteField = frame.paste,
                    keyDownField = frame.keys
                )
            }
        }
    }

    @Serializable
    public data class Username(
        @SerialName("v")
        public override val version: String,
        @SerialName("appLang")
        override val appLanguage: String,
        @SerialName("timezone")
        override val timezone: String,
        @SerialName("deviceName")
        override val deviceName: Long,
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
        @SerialName("keyboards")
        override val keyboards: List<String>,
        @SerialName("frame")
        val frame: FrameType,
        @SerialName("timeUsername")
        val timeOnField: List<Int>,
        @SerialName("clickUsername")
        val clickOnField: Int,
        @SerialName("copyUsername")
        val copyField: List<String>,
        @SerialName("pasteUsername")
        val pasteField: List<String>,
        @SerialName("keydownUsername")
        val keyDownField: List<String>,
    ) : ChallengeFrame() {
        public companion object {
            public suspend fun from(context: Context, frame: ChallengeFrameDetails?): Username? {
                if (frame == null) return null
                return Username(
                    version = CHALLENGE_VERSION,
                    appLanguage = appLanguage(),
                    timezone = deviceTimezone(),
                    deviceName = deviceModelName(),
                    regionCode = context.deviceRegion(),
                    timezoneOffset = deviceTimezoneOffset(),
                    rooted = isDeviceRooted(context),
                    fontSize = context.deviceFontSize().toString(),
                    storage = context.deviceStorage(),
                    darkMode = context.nightMode(),
                    keyboards = context.deviceInputMethods(),
                    frame = FrameType(frame.challengeFrame),
                    timeOnField = frame.focusTime,
                    clickOnField = frame.clicks,
                    copyField = frame.copy,
                    pasteField = frame.paste,
                    keyDownField = frame.keys
                )
            }
        }
    }
}
