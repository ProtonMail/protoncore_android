package me.proton.core.accountrecovery.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.accountrecovery.domain.IsAccountRecoveryEnabled
import javax.inject.Inject

public class IsAccountRecoveryEnabledImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : IsAccountRecoveryEnabled {
    override fun invoke(): Boolean {
        return context.resources.getBoolean(R.bool.core_feature_account_recovery_enabled)
    }
}
