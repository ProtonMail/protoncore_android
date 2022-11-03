package me.proton.core.usersettings.domain

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.usersettings.domain.entity.DeviceSettings
import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import me.proton.core.util.kotlin.CoreLogger
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceSettingsHandler @Inject constructor(
    internal val scopeProvider: CoroutineScopeProvider,
    internal val deviceSettingsRepository: DeviceSettingsRepository,
)

fun DeviceSettingsHandler.onDeviceSettingsChanged(
    block: suspend (DeviceSettings) -> Unit,
) = deviceSettingsRepository.observeDeviceSettings()
    .onEach { settings -> block(settings) }
    .catch { CoreLogger.e(LogTag.DEFAULT, it) }
    .launchIn(scopeProvider.GlobalDefaultSupervisedScope)

object LogTag {
    /** Default tag for any other issue we need to log */
    const val DEFAULT = "core.usersettings.default"
}
