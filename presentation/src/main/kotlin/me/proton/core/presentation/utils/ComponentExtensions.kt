package me.proton.core.presentation.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

inline fun <reified T : Activity> Context.setComponentSettings(enabled: Boolean? = null) {
    packageManager.setComponentEnabledSetting(
        ComponentName(this, T::class.java),
        when (enabled) {
            null -> PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            true -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            false -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        },
        PackageManager.DONT_KILL_APP
    )
}
