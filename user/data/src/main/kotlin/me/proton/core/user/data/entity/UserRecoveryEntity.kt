package me.proton.core.user.data.entity

import androidx.room.Entity
import me.proton.core.network.domain.session.SessionId

@Entity
data class UserRecoveryEntity(
    val state: Int,
    val startTime: Long,
    val endTime: Long,
    val sessionId: SessionId,
    val reason: Int?
)
