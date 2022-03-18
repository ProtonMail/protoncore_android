/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.humanverification.presentation.ui.hv2.verification

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.ui.hv2.HV2DialogFragment
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationCode
import me.proton.core.presentation.ui.view.Loadable
import me.proton.core.presentation.viewmodel.ViewModelResult
import me.proton.core.util.kotlin.exhaustive

/**
 * Base class for all verification methods.
 *
 * @author Dino Kadrikj.
 */
internal class HumanVerificationMethodCommon(
    val viewModel: HumanVerificationCode,
    val urlToken: String,
    val tokenType: TokenType
) {

    /**
     * Observes the verification code result.
     *
     * @param owner the lifecycle owner for the LiveData.
     * @param parentFragmentManager the parent [FragmentManager]  which is needed for passing back
     * the result from the verification method fragment back to the parent fragment.
     * @param onVerificationCodeError the block of code to be executed on error.
     */
    fun onViewCreated(
        owner: LifecycleOwner,
        parentFragmentManager: FragmentManager,
        loadable: Loadable? = null,
        onVerificationCodeError: (Throwable?) -> Unit
    ) {
        viewModel.verificationCodeStatus
            .flowWithLifecycle(owner.lifecycle)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is ViewModelResult.None,
                    is ViewModelResult.Processing -> Unit
                    is ViewModelResult.Error -> {
                        loadable?.loadingComplete()
                        onVerificationCodeError(it.throwable)
                    }
                    is ViewModelResult.Success -> {
                        loadable?.loadingComplete()
                        onGetCodeClicked(it.value, parentFragmentManager)
                    }
                }.exhaustive
            }.launchIn(owner.lifecycleScope)
    }

    /**
     * Passes the result of the verification method back to the parent for handling.
     * Usually this is invoked on successful verification code sent to the entered destination
     * (email, phone) or if the user has already the code from other source
     * (ex. customer support for manual verification).
     */
    fun onGetCodeClicked(destination: String? = null, parentFragmentManager: FragmentManager) {
        parentFragmentManager.setFragmentResult(
            HV2DialogFragment.KEY_PHASE_TWO,
            bundleOf(
                HV2DialogFragment.ARG_DESTINATION to destination,
                HV2DialogFragment.ARG_TOKEN_TYPE to tokenType.value
            )
        )
    }

    companion object {
        const val ARG_URL_TOKEN = "arg.urltoken"
    }
}
