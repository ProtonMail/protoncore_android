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

package me.proton.core.auth.presentation.ui

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.FragmentAddAccountBinding
import me.proton.core.auth.presentation.entity.AddAccountInput
import me.proton.core.presentation.ui.ProtonFragment
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.viewBinding
import me.proton.core.telemetry.domain.entity.TelemetryPriority
import me.proton.core.telemetry.presentation.UiComponentProductMetricsDelegateOwner
import me.proton.core.telemetry.presentation.annotation.ProductMetrics
import me.proton.core.telemetry.presentation.annotation.ScreenClosed
import me.proton.core.telemetry.presentation.annotation.ScreenDisplayed
import me.proton.core.telemetry.presentation.annotation.ViewClicked
import java.lang.ref.WeakReference

@AndroidEntryPoint
@ProductMetrics(
    group = "account.any.signup",
    flow = "mobile_signup_full"
)
@ScreenDisplayed(
    event = "fe.add_account.displayed",
    priority = TelemetryPriority.Immediate
)
@ScreenClosed(
    event = "user.add_account.closed",
    priority = TelemetryPriority.Immediate
)
@ViewClicked(
    event = "user.add_account.clicked",
    viewIds = ["sign_up", "sign_in"],
    priority = TelemetryPriority.Immediate
)
internal class AddAccountFragment :
    ProtonFragment(R.layout.fragment_add_account),
    UiComponentProductMetricsDelegateOwner {

    private val binding by viewBinding(FragmentAddAccountBinding::bind)

    private val input by lazy {
        requireNotNull(requireArguments().getParcelable<AddAccountInput>(ARG_ADD_ACCOUNT_INPUT))
    }

    private val addAccountActivity by lazy { WeakReference(activity as AddAccountActivity) }
    private val authOrchestrator by lazy { addAccountActivity.get()?.authOrchestrator }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // A Lottie animation will be added in the next iteration.
        // binding.lottie.addAnimatorUpdateListener {
        //     it.doOnEnd { binding.lottie.animate().alpha(0f).setListener(null) }
        // }

        binding.signIn.onClick {
            authOrchestrator?.startLoginWorkflow(input.username)
        }
        binding.signUp.onClick {
            authOrchestrator?.startSignupWorkflow()
        }
    }

    companion object {
        private const val ARG_ADD_ACCOUNT_INPUT = "ARG_ADD_ACCOUNT_INPUT"

        operator fun invoke(input: AddAccountInput) = AddAccountFragment().apply {
            arguments = bundleOf(ARG_ADD_ACCOUNT_INPUT to input)
        }
    }
}
