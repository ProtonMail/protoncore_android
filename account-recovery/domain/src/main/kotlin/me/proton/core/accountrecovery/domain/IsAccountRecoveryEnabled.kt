package me.proton.core.accountrecovery.domain

public interface IsAccountRecoveryEnabled {
    public operator fun invoke(): Boolean
}
