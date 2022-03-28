/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.auth.presentation.ui.signup

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.ui.removeCreatingUser
import me.proton.core.auth.presentation.viewmodel.signup.SignupViewModel
import me.proton.core.humanverification.presentation.HumanVerificationManagerObserver
import me.proton.core.humanverification.presentation.onHumanVerificationFailed
import me.proton.core.humanverification.presentation.utils.hasHumanVerificationFragment

class CreatingUserFragment : SignupFragment(R.layout.fragment_creating_user) {

    private val signupViewModel by activityViewModels<SignupViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val humanVerificationObserver = signupViewModel.observeHumanVerification(requireActivity().lifecycle)
        observeHumanVerificationFailed(humanVerificationObserver)
    }

    private fun observeHumanVerificationFailed(observer: HumanVerificationManagerObserver) {
        // If we receive a HV failed state and HV screen is shown it means the user canceled it so we need to remove it.
        observer.onHumanVerificationFailed(initialState = false) {
            if (parentFragmentManager.hasHumanVerificationFragment()) {
                parentFragmentManager.removeCreatingUser()
                // Stop observing to avoid duplicate callback calls
                signupViewModel.stopObservingHumanVerification(true)
            }
        }
    }

    override fun onBackPressed() {
        parentFragmentManager.popBackStack()
        // Stop observing, the user went back so they want to cancel the registration and there's no point showing the
        // HV screen in that case
        signupViewModel.stopObservingHumanVerification(false)
    }
}
