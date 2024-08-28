package me.proton.core.auth.presentation.compose

public sealed interface DeviceSecretViewState {
    public data object Idle : DeviceSecretViewState
    public data object Close : DeviceSecretViewState
}
