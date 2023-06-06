package me.proton.core.user.data.extension

import me.proton.core.key.data.api.response.UserRecoveryResponse
import me.proton.core.network.domain.session.SessionId
import me.proton.core.user.data.entity.UserRecoveryEntity
import me.proton.core.user.domain.entity.UserRecovery

internal fun UserRecoveryResponse.toUserRecovery() = UserRecovery(
    state = UserRecovery.State.enumOf(state),
    startTime = startTime,
    endTime = endTime,
    sessionId = SessionId(sessionId),
    reason = UserRecovery.Reason.map[reason]
)

internal fun UserRecoveryEntity.toUserRecovery() = UserRecovery(
    state = UserRecovery.State.enumOf(state),
    startTime = startTime,
    endTime = endTime,
    sessionId = sessionId,
    reason = UserRecovery.Reason.map[reason]
)

internal fun UserRecovery.toUserRecoveryEntity() = UserRecoveryEntity(
    state = state.value,
    startTime = startTime,
    endTime = endTime,
    sessionId = sessionId,
    reason = reason?.value
)
