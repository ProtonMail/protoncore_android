package me.proton.core.observability.domain.metrics.common

@Suppress("EnumEntryName", "EnumNaming")
public enum class UnlockUserStatus {
    success,
    noPrimaryKey,
    noKeySaltsForPrimaryKey,
    primaryKeyInvalidPassphrase
}
