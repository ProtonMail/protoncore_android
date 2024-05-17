/*
 * Copyright (c) 2024 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.auth.presentation.alert.confirmpass

import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.tabs.TabLayout
import me.proton.core.auth.domain.entity.SecondFactorMethod
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.DialogConfirmPasswordBinding
import me.proton.core.auth.presentation.util.setTextWithAnnotatedLink
import me.proton.core.auth.presentation.viewmodel.ConfirmPasswordDialogViewModel
import me.proton.core.presentation.utils.onClick

internal class ConfirmPasswordDialogViewController(
    private val binding: DialogConfirmPasswordBinding,
    lifecycleOwner: LifecycleOwner,
    private val onEnterButtonClick: (SecondFactorMethod?) -> Unit,
    private val onCancelButtonClick: () -> Unit,
    private val onSecurityKeyInfoClick: () -> Unit,
) : DefaultLifecycleObserver {
    val password: String? get() = binding.password.text?.toString()
    val root: View get() = binding.root
    val twoFactorCode: String? get() = binding.twoFA.text?.toString()

    private var selectedSecondFactorMethod: SecondFactorMethod? = null
        private set(value) {
            field = value
            onSecondFactorMethodSelected()
        }

    private val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            val tag = tab?.tag?.takeIf { binding.twoFaTabs.isVisible }
            selectedSecondFactorMethod = tag as? SecondFactorMethod
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
        override fun onTabReselected(tab: TabLayout.Tab?) = onTabSelected(tab)
    }

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        binding.enterButton.onClick {
            onEnterButtonClick(selectedSecondFactorMethod)
        }
        binding.cancelButton.onClick(onCancelButtonClick)
        binding.securityKeyInfo.setTextWithAnnotatedLink(R.string.auth_2fa_insert_security_key, "more") {
            onSecurityKeyInfoClick()
        }
        binding.twoFaTabs.addOnTabSelectedListener(tabSelectedListener)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        binding.twoFaTabs.removeOnTabSelectedListener(tabSelectedListener)
        super.onDestroy(owner)
    }

    fun selectSecondFactorMethodTab(method: SecondFactorMethod?) {
        val tab = findTab(method)
        binding.twoFaTabs.selectTab(tab)
    }

    fun setIdle() {
        binding.enterButton.setIdle()
    }

    fun setLoading() {
        binding.enterButton.setLoading()
    }

    fun setSecondFactorResult(state: ConfirmPasswordDialogViewModel.State.SecondFactorResult) {
        binding.enterButton.setIdle()
        binding.twoFaContainer.isVisible = state.methods.isNotEmpty()

        val firstSecondFactorMethod = state.methods.firstOrNull()
        if (state.methods.size > 1) {
            binding.twoFaTabs.isVisible = true
            state.methods
                .filter { findTab(it) == null }
                .forEach { twoFaMethod ->
                    binding.twoFaTabs.apply {
                        addTab(newTab().apply {
                            text = context.getString(twoFaMethod.tabTitleRes())
                            tag = twoFaMethod
                        })
                    }
                }
            selectSecondFactorMethodTab(firstSecondFactorMethod)
        } else {
            binding.twoFaTabs.isVisible = false
            selectedSecondFactorMethod = firstSecondFactorMethod
        }
    }

    private fun findTab(method: SecondFactorMethod?): TabLayout.Tab? {
        return (0 until binding.twoFaTabs.tabCount).mapNotNull {
            binding.twoFaTabs.getTabAt(it)
        }.firstOrNull { it.tag == method }
    }

    private fun onSecondFactorMethodSelected() = when (selectedSecondFactorMethod) {
        SecondFactorMethod.Authenticator -> {
            binding.securityKeyContainer.isVisible = true
            binding.oneTimeCodeContainer.isVisible = false
        }

        SecondFactorMethod.Totp -> {
            binding.securityKeyContainer.isVisible = false
            binding.oneTimeCodeContainer.isVisible = true
        }

        else -> {
            binding.securityKeyContainer.isVisible = false
            binding.oneTimeCodeContainer.isVisible = false
        }
    }
}

private fun SecondFactorMethod.tabTitleRes(): Int = when (this) {
    SecondFactorMethod.Totp -> R.string.auth_2fa_tab_one_time_code
    SecondFactorMethod.Authenticator -> R.string.auth_2fa_tab_security_key
}
