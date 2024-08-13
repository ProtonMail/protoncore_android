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

package me.proton.core.auth.presentation.alert.confirmpass

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.result.ActivityResultCaller
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.auth.domain.entity.SecondFactorMethod
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey
import me.proton.core.auth.presentation.R
import me.proton.core.auth.presentation.databinding.DialogConfirmPasswordBinding
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordInput
import me.proton.core.auth.presentation.entity.confirmpass.ConfirmPasswordResult
import me.proton.core.auth.presentation.ui.handle
import me.proton.core.auth.presentation.viewmodel.ConfirmPasswordDialogViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.scopes.MissingScopeState
import me.proton.core.network.domain.scopes.Scope
import me.proton.core.presentation.utils.ProtectScreenConfiguration
import me.proton.core.presentation.utils.ScreenContentProtector
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.openBrowserLink
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

/**
 * This dialog handles only [Scope.PASSWORD] or [Scope.LOCKED]. Any other scope will be ignored.
 */
@AndroidEntryPoint
class ConfirmPasswordDialog : DialogFragment() {
    @Inject
    lateinit var performTwoFaWithSecurityKey: Optional<PerformTwoFaWithSecurityKey<ActivityResultCaller, Activity>>

    private val viewModel by viewModels<ConfirmPasswordDialogViewModel>()

    private val screenProtector = ScreenContentProtector(ProtectScreenConfiguration())

    private val input: ConfirmPasswordInput by lazy {
        requireNotNull(requireArguments().getParcelable(ARG_INPUT))
    }

    private val missingScopes: List<Scope> by lazy {
        input.missingScopes.mapNotNull {
            Scope.getByValue(it)
        }
    }

    private val missingScope: Scope by lazy {
        // if we are required password scope we will try to obtain only that one
        when {
            missingScopes.contains(Scope.PASSWORD) -> Scope.PASSWORD
            missingScopes.contains(Scope.LOCKED) -> Scope.LOCKED
            else -> throw IllegalArgumentException("Unrecognized scope!")
        }
    }

    private val userId: UserId by lazy {
        UserId(input.userId)
    }

    private val viewController by lazy {
        ConfirmPasswordDialogViewController(
            DialogConfirmPasswordBinding.inflate(layoutInflater),
            lifecycleOwner = this,
            onEnterButtonClick = this::onEnterButtonClick,
            onCancelButtonClick = { setResultAndDismiss(null) },
            onSecurityKeyInfoClick = {
                context?.let {
                    it.openBrowserLink(it.getString(R.string.confirm_password_2fa_security_key))
                }
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performTwoFaWithSecurityKey.getOrNull()?.register(requireActivity(), this::onTwoFaWithSecurityKeyResult)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        screenProtector.protect(requireActivity())

        val alertDialog = buildAlertDialog()

        viewModel.state
            .flowWithLifecycle(lifecycle)
            .onEach(this::handleState)
            .launchIn(lifecycleScope)
        viewModel.checkForSecondFactorInput(userId, missingScope)

        return alertDialog.apply {
            setCanceledOnTouchOutside(false)
        }
    }

    private fun buildAlertDialog(): AlertDialog = MaterialAlertDialogBuilder(requireContext())
        .setTitle(R.string.presentation_signin_to_continue)
        .setOnKeyListener { _, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP && !keyEvent.isCanceled) {
                setResultAndDismiss(null)
                true
            } else false
        }
        .setView(viewController.root)
        .create()

    private fun onEnterButtonClick(selectedSecondFactorMethod: SecondFactorMethod?) {
        val password = viewController.password.orEmpty()
        when (missingScope) {
            Scope.PASSWORD -> when (selectedSecondFactorMethod) {
                SecondFactorMethod.Totp -> onTotpSubmitted()
                SecondFactorMethod.Authenticator -> onSecurityKeySubmitted()
                null -> viewModel.unlock(userId, missingScope, password)
            }

            Scope.LOCKED -> viewModel.unlock(userId, missingScope, password)
        }
    }

    private fun onTotpSubmitted() {
        val password = viewController.password.orEmpty()
        val twoFactorCode = viewController.twoFactorCode.orEmpty()
        viewModel.unlock(userId, missingScope, password, secondFactorProof = SecondFactorProof.SecondFactorCode(twoFactorCode))
    }

    private fun onSecurityKeySubmitted() {
        val performTwoFaWithSecurityKey = performTwoFaWithSecurityKey.getOrNull() ?: return
        val requestOptions = requireNotNull(viewModel.fido2Info?.authenticationOptions?.publicKey)

        viewController.setLoading()

        lifecycleScope.launch {
            val activity = activity ?: return@launch

            val launchResult = performTwoFaWithSecurityKey.invoke(activity, requestOptions)
            viewModel.onLaunchResult(launchResult)

            when (launchResult) {
                is PerformTwoFaWithSecurityKey.LaunchResult.Failure -> {
                    viewController.setIdle()
                    viewController.root.errorSnack(
                        message = launchResult.exception.localizedMessage
                            ?: getString(R.string.auth_login_general_error)
                    )
                }

                is PerformTwoFaWithSecurityKey.LaunchResult.Success -> Unit
            }
        }
    }

    private fun handleState(state: ConfirmPasswordDialogViewModel.State) = when (state) {
        is ConfirmPasswordDialogViewModel.State.Success -> setResultAndDismiss(state.state)
        is ConfirmPasswordDialogViewModel.State.ProcessingObtainScope -> viewController.setLoading()
        is ConfirmPasswordDialogViewModel.State.ProcessingSecondFactor -> viewController.setLoading()
        is ConfirmPasswordDialogViewModel.State.Idle -> viewController.setIdle()
        is ConfirmPasswordDialogViewModel.State.SecondFactorResult -> viewController.setSecondFactorResult(state)
        is ConfirmPasswordDialogViewModel.State.Error.InvalidAccount -> {
            viewController.setIdle()
            context.errorToast(getString(R.string.auth_account_not_found_error))
        }

        is ConfirmPasswordDialogViewModel.State.Error.Unknown,
        is ConfirmPasswordDialogViewModel.State.Error.General -> {
            setResultAndDismiss(MissingScopeState.ScopeObtainFailed)
            viewController.setIdle()
        }
    }

    private fun setResultAndDismiss(state: MissingScopeState?) {
        val obtained = state is MissingScopeState.ScopeObtainSuccess

        viewModel.onConfirmPasswordResult(state).invokeOnCompletion {
            parentFragmentManager.setFragmentResult(
                CONFIRM_PASS_SET,
                bundleOf(
                    BUNDLE_CONFIRM_PASS_DATA to ConfirmPasswordResult(obtained)
                )
            )
            dismissAllowingStateLoss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        screenProtector.unprotect(requireActivity())
    }

    private fun onTwoFaWithSecurityKeyResult(
        result: PerformTwoFaWithSecurityKey.Result,
        options: Fido2PublicKeyCredentialRequestOptions
    ) {
        viewController.setIdle()
        viewModel.onSignResult(result)

        result.handle(requireContext(), viewController.root) { resultSuccess ->
            onSecurityKeyAuthSuccess(resultSuccess, options)
        }
    }

    private fun onSecurityKeyAuthSuccess(
        result: PerformTwoFaWithSecurityKey.Result.Success,
        options: Fido2PublicKeyCredentialRequestOptions
    ) {
        val password = viewController.password.orEmpty()
        viewModel.unlock(
            userId = userId,
            missingScope = missingScope,
            password = password,
            secondFactorProof = SecondFactorProof.Fido2(
                publicKeyOptions = options,
                clientData = result.response.clientDataJSON,
                authenticatorData = result.response.authenticatorData,
                signature = result.response.signature,
                credentialID = result.rawId
            )
        )
    }

    companion object {
        private const val ARG_INPUT = "arg.confirmPasswordInput"

        const val CONFIRM_PASS_SET = "key.confirm_pass_set"
        const val BUNDLE_CONFIRM_PASS_DATA = "bundle.confirm_pass_data"

        operator fun invoke(
            input: ConfirmPasswordInput
        ) = ConfirmPasswordDialog().apply {
            arguments = bundleOf(
                ARG_INPUT to input
            )
        }
    }
}
