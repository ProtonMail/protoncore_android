package me.proton.core.eventmanager.domain

import me.proton.core.domain.entity.UserId

interface IsCoreEventManagerEnabled {
    operator fun invoke(userId: UserId): Boolean
}
