package me.proton.core.humanverification.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by dinokadrikj on 6/15/20.
 */
class HumanVerificationViewModelFactory(private val availableVerificationMethods: List<String>) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T = HumanVerificationViewModel(availableVerificationMethods) as T
}
