/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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

package me.proton.core.humanverification.presentation.ui.verification

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.presentation.ui.HumanVerificationDialogFragment
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationCode
import me.proton.core.presentation.ui.view.Loadable
import me.proton.core.presentation.viewmodel.onError
import me.proton.core.presentation.viewmodel.onSuccess

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

    private var destination: String? = null

    var verificationToken: String? = null

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
        onVerificationCodeError: () -> Unit
    ) {
        viewModel.verificationCodeStatus.onSuccess {
            onGetCodeClicked(parentFragmentManager)
        }.onError {
            loadable?.loadingComplete()
            onVerificationCodeError()
        }.launchIn(owner.lifecycleScope)
    }

    /**
     * Passes the result of the verification method back to the parent for handling.
     * Usually this is invoked on successful verification code sent to the entered destination
     * (email, phone) or if the user has already the code from other source
     * (ex. customer support for manual verification).
     */
    fun onGetCodeClicked(parentFragmentManager: FragmentManager) {
        parentFragmentManager.setFragmentResult(
            HumanVerificationDialogFragment.KEY_PHASE_TWO,
            bundleOf(
                HumanVerificationDialogFragment.ARG_DESTINATION to destination,
                HumanVerificationDialogFragment.ARG_TOKEN_TYPE to tokenType.tokenTypeValue
            )
        )
    }

    companion object {
        const val ARG_URL_TOKEN = "arg.urltoken"
    }
}
