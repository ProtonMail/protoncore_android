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

package me.proton.core.auth.presentation.alert

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.activity.result.ActivityResultCaller
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.auth.fido.domain.entity.Fido2PublicKeyCredentialRequestOptions
import me.proton.core.auth.fido.domain.entity.SecondFactorProof
import me.proton.core.auth.fido.domain.usecase.PerformTwoFaWithSecurityKey
import me.proton.core.auth.presentation.databinding.Dialog2faInputBinding
import me.proton.core.auth.presentation.entity.SecondFactorProofEntity
import me.proton.core.auth.presentation.entity.TwoFAMechanisms
import me.proton.core.auth.presentation.entity.toEntity
import me.proton.core.auth.presentation.ui.handle
import me.proton.core.auth.presentation.util.setTextWithAnnotatedLink
import me.proton.core.auth.presentation.viewmodel.Source
import me.proton.core.auth.presentation.viewmodel.TwoFAInputDialogViewModel
import me.proton.core.domain.entity.UserId
import me.proton.core.presentation.R
import me.proton.core.presentation.utils.ProtectScreenConfiguration
import me.proton.core.presentation.utils.ScreenContentProtector
import me.proton.core.presentation.utils.SnackbarLength
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.errorToast
import me.proton.core.presentation.utils.onClick
import me.proton.core.presentation.utils.openBrowserLink
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

@AndroidEntryPoint
class TwoFAInputDialog : DialogFragment(), TabLayout.OnTabSelectedListener {

    @Inject
    lateinit var performTwoFaWithSecurityKey: Optional<PerformTwoFaWithSecurityKey<ActivityResultCaller, Activity>>

    private val viewModel by viewModels<TwoFAInputDialogViewModel>()

    companion object {
        private const val ARG_USER_ID = "arg.userId"
        private const val ARG_SOURCE = "arg.source"
        const val KEY_2FA_SET = "key.2fa_set"
        const val BUNDLE_KEY_2FA_DATA = "bundle.2fa_data"

        operator fun invoke(source: Source, userId: String) = TwoFAInputDialog().apply {
            arguments = bundleOf(
                ARG_SOURCE to source,
                ARG_USER_ID to userId
            )
        }
    }

    private val screenProtector = ScreenContentProtector(ProtectScreenConfiguration())

    private val userId by lazy {
        UserId(requireNotNull(requireArguments().getString(ARG_USER_ID)))
    }

    private val source by lazy {
        requireArguments().let {
            it.classLoader = Source::class.java.classLoader
            requireNotNull(it.getParcelable<Source>(ARG_SOURCE))
        }
    }

    private val binding by lazy {
        Dialog2faInputBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performTwoFaWithSecurityKey.getOrNull()?.register(requireActivity(), ::onTwoFaWithSecurityKeyResult)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        screenProtector.protect(requireActivity())

        binding.tabLayout.addOnTabSelectedListener(this@TwoFAInputDialog)

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.presentation_authenticate)
            // passing null to the listeners is a workaround to prevent the dialog to auto-dismiss on button click
            .setPositiveButton(R.string.presentation_alert_enter, null)
            .setNegativeButton(R.string.presentation_alert_cancel, null)
            .setView(binding.root)
            .create()

        viewModel.state
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChanged()
            .onEach {
                when (it) {
                    is TwoFAInputDialogViewModel.State.Idle -> with(binding) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                        progressLayout.isVisible = false
                        tabLayout.isVisible = it.showSecurityKey
                        if (it.showSecurityKey) {
                            selectSecurityKeyTab()
                        } else {
                            selectOneTimeCodeTab()
                        }
                    }

                    is TwoFAInputDialogViewModel.State.Loading -> {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        binding.oneTimeCodeGroup.isVisible = false
                        binding.securityKeyGroup.isVisible = false
                        binding.tabLayout.isVisible = false
                        binding.progressLayout.isVisible = true
                    }

                    is TwoFAInputDialogViewModel.State.Error.InvalidAccount -> {
                        requireContext().errorToast(getString(R.string.presentation_error_general))
                        dismissAllowingStateLoss()
                    }

                    is TwoFAInputDialogViewModel.State.Error.SetupError -> binding.root.errorSnack(
                        R.string.presentation_error_general,
                        length = SnackbarLength.INDEFINITE
                    ) {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        binding.progressLayout.isVisible = false
                        setAction(R.string.presentation_retry) {
                            viewModel.setup(userId)
                        }
                    }
                }
            }.launchIn(lifecycleScope)

        return alertDialog.apply {
            setOnShowListener {
                viewModel.setup(userId)

                // workaround to prevent the dialog to auto-dismiss on button click
                getButton(AlertDialog.BUTTON_POSITIVE).apply {
                    isAllCaps = false
                    onClick {
                        when (selectedTwoFAOption()) {
                            TwoFAMechanisms.SECURITY_KEY -> onSecurityKeySubmitted()
                            TwoFAMechanisms.ONE_TIME_CODE -> onOneTimeCodeSubmitted()
                        }
                    }
                }
                getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                    isAllCaps = false
                    onClick {
                        parentFragmentManager.setFragmentResult(KEY_2FA_SET, bundleOf())

                        dismissAllowingStateLoss()
                    }
                }
            }
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        screenProtector.unprotect(requireActivity())
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.position) {
            TwoFAMechanisms.SECURITY_KEY.ordinal -> selectSecurityKeyTab()
            TwoFAMechanisms.ONE_TIME_CODE.ordinal -> selectOneTimeCodeTab()
        }
    }

    override fun onTabUnselected(p0: TabLayout.Tab?) = Unit

    override fun onTabReselected(p0: TabLayout.Tab?) = Unit

    private fun onTwoFaWithSecurityKeyResult(
        result: PerformTwoFaWithSecurityKey.Result,
        options: Fido2PublicKeyCredentialRequestOptions
    ) {
        viewModel.onSignResult(source, result)
        result.handle(requireContext(), binding.root) { resultSuccess ->
            val secondFactorFido = SecondFactorProof.Fido2(
                publicKeyOptions = options,
                clientData = resultSuccess.response.clientDataJSON,
                authenticatorData = resultSuccess.response.authenticatorData,
                signature = resultSuccess.response.signature,
                credentialID = resultSuccess.rawId
            )

            parentFragmentManager.setFragmentResult(
                KEY_2FA_SET, bundleOf(
                    BUNDLE_KEY_2FA_DATA to SecondFactorProofEntity.Fido2Entity(
                        secondFactorFido.publicKeyOptions.toEntity(),
                        secondFactorFido.clientData,
                        secondFactorFido.authenticatorData,
                        secondFactorFido.signature,
                        secondFactorFido.credentialID
                    )
                )
            )
        }
    }

    private fun selectSecurityKeyTab() = with(binding) {
        oneTimeCodeGroup.isVisible = false
        securityKeyGroup.isVisible = true

        securityKeyText.setTextWithAnnotatedLink(
            me.proton.core.auth.presentation.R.string.auth_2fa_insert_security_key,
            "more"
        ) {
            context?.openBrowserLink(getString(me.proton.core.auth.presentation.R.string.confirm_password_2fa_security_key))
        }
    }

    private fun selectOneTimeCodeTab() = with(binding) {
        oneTimeCodeGroup.isVisible = true
        securityKeyGroup.isVisible = false
    }

    private fun selectedTwoFAOption(): TwoFAMechanisms =
        if (binding.tabLayout.isVisible) {
            TwoFAMechanisms.enumOf(binding.tabLayout.selectedTabPosition)
        } else TwoFAMechanisms.ONE_TIME_CODE

    private fun onOneTimeCodeSubmitted() {
        parentFragmentManager.setFragmentResult(
            KEY_2FA_SET, bundleOf(
                BUNDLE_KEY_2FA_DATA to SecondFactorProofEntity.SecondFactorCodeEntity(binding.twoFA.text.toString())
            )
        )
        dismissAllowingStateLoss()
    }

    private fun onSecurityKeySubmitted() {
        val performTwoFaWithSecurityKey = performTwoFaWithSecurityKey.getOrNull() ?: return
        val requestOptions = requireNotNull(viewModel.fido2Info?.authenticationOptions?.publicKey)

        lifecycleScope.launch {
            val launchResult = performTwoFaWithSecurityKey.invoke(requireActivity(), requestOptions)
            viewModel.onLaunchResult(source, launchResult)
            when (launchResult) {
                is PerformTwoFaWithSecurityKey.LaunchResult.Failure -> {
                    binding.root.errorSnack(
                        message = launchResult.exception.localizedMessage
                            ?: getString(me.proton.core.auth.presentation.R.string.auth_login_general_error)
                    )
                }

                is PerformTwoFaWithSecurityKey.LaunchResult.Success -> Unit
            }
        }
    }
}
