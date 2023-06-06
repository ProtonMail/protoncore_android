package me.proton.core.user.domain.entity

import me.proton.core.domain.type.IntEnum
import me.proton.core.network.domain.session.SessionId

data class UserRecovery(
    val state: IntEnum<State>,
    val startTime: Long,
    val endTime: Long,
    val sessionId: SessionId,
    val reason: Reason?
) {
    enum class State(val value: Int) {
        None(0),
        Grace(1),
        Cancelled(2),
        Insecure(3),
        Expired(4);

        companion object {
            val map = values().associateBy { it.value }
            fun enumOf(value: Int): IntEnum<State> = IntEnum(value, map[value])
        }
    }

    enum class Reason(val value: Int) {
        None(0),
        Cancelled(1),
        Authentication(2);

        companion object {
            val map = values().associateBy { it.value }
        }
    }
}
