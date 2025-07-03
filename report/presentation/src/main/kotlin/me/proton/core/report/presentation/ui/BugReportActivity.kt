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

package me.proton.core.report.presentation.ui

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.enableProtonEdgeToEdge
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.showKeyboard
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportField
import me.proton.core.report.domain.entity.BugReportValidationError
import me.proton.core.report.domain.usecase.SendBugReport
import me.proton.core.report.presentation.R
import me.proton.core.report.presentation.databinding.CoreReportActivityBugReportBinding
import me.proton.core.report.presentation.entity.BugReportFormState
import me.proton.core.report.presentation.entity.BugReportInput
import me.proton.core.report.presentation.entity.BugReportOutput
import me.proton.core.report.presentation.entity.ExitSignal
import me.proton.core.report.presentation.entity.ReportFormData
import me.proton.core.report.presentation.viewmodel.BugReportViewModel
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
internal class BugReportActivity : ProtonViewBindingActivity<CoreReportActivityBugReportBinding>(
    CoreReportActivityBugReportBinding::inflate
) {
    private var exitDialog: AlertDialog? = null
    private val input: BugReportInput by lazy {
        intent.getParcelableExtra<BugReportInput>(INPUT_BUG_REPORT) ?: error("Missing activity input")
    }

    private val viewModel by viewModels<BugReportViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableProtonEdgeToEdge()
        super.onCreate(savedInstanceState)
        initToolbar()
        initForm()
        listenForViewModelFlows()
        addOnBackPressedCallback { viewModel.tryExit(getReportFormData()) }
        binding.bugReportSubject.requestFocus()
    }

    override fun onDestroy() {
        exitDialog?.dismiss()
        exitDialog = null
        super.onDestroy()
    }

    private fun initForm() {
        binding.bugReportDescription.filters = arrayOf(InputFilter.LengthFilter(BugReport.DescriptionMaxLength))
        binding.bugReportSubject.filters = arrayOf(InputFilter.LengthFilter(BugReport.SubjectMaxLength))

        binding.bugReportDescription.setOnFocusChangeListener { _, hasFocus ->
            lifecycleScope.launch {
                if (hasFocus) {
                    viewModel.clearFormErrors(BugReportField.Description)
                } else {
                    viewModel.revalidateDescription(binding.bugReportDescription.text?.toString() ?: "")
                }
            }
        }

        binding.bugReportSubject.setOnFocusChangeListener { _, hasFocus ->
            lifecycleScope.launch {
                if (hasFocus) {
                    viewModel.clearFormErrors(BugReportField.Subject)
                } else {
                    viewModel.revalidateSubject(binding.bugReportSubject.text?.toString() ?: "")
                }
            }
        }

        binding.spacer.setOnClickListener {
            if (binding.bugReportDescription.hasFocus()) {
                hideKeyboard(binding.bugReportDescription)
            } else {
                showKeyboard(binding.bugReportDescription)
            }
        }

        binding.bugReportAttachLogLayout.visibility = if (viewModel.shouldShowAttachLog) View.VISIBLE else View.GONE
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.bug_report_send) {
                viewModel.trySendingBugReport(
                    getReportFormData(),
                    email = input.email,
                    username = input.username,
                    country = input.country,
                    isp = input.isp
                )
                true
            } else false
        }
    }

    private fun listenForViewModelFlows() {
        viewModel.bugReportFormState
            .flowWithLifecycle(lifecycle)
            .onEach(this::handleBugReportFormState)
            .launchIn(lifecycleScope)

        viewModel.exitSignal
            .flowWithLifecycle(lifecycle)
            .onEach(this::handleExitSignal)
            .launchIn(lifecycleScope)

        viewModel.hideKeyboardSignal
            .flowWithLifecycle(lifecycle)
            .onEach { hideKeyboard() }
            .launchIn(lifecycleScope)
    }

    private fun getReportFormData(): ReportFormData {
        return ReportFormData(
            subject = binding.bugReportSubject.text.toString(),
            description = binding.bugReportDescription.text.toString(),
            attachLog = binding.bugReportAttachLog.isVisible && binding.bugReportAttachLog.isChecked,
        )
    }

    private fun handleBugReportFormState(state: BugReportFormState) {
        when (state) {
            BugReportFormState.Idle -> setFormState(isLoading = false)
            BugReportFormState.Processing -> setFormState(isLoading = true)
            is BugReportFormState.FormError -> {
                setFormState(isLoading = false)
                state.errors.forEach(this::handleFormError)
            }
            is BugReportFormState.SendingResult -> {
                setFormState(isLoading = true)
                handleSendingResult(state)
            }
        }.exhaustive
    }

    private fun handleExitSignal(exitSignal: ExitSignal) {
        when (exitSignal) {
            ExitSignal.Ask -> showExitDialog()
            ExitSignal.BugReportEnqueued -> reportSuccessfullyCompleted()
            ExitSignal.ExitNow -> finish()
        }.exhaustive
    }

    private fun handleFormError(validationError: BugReportValidationError) {
        val (textView, message) = when (validationError) {
            BugReportValidationError.DescriptionMissing ->
                binding.bugReportDescriptionLayout to resources.getQuantityString(
                    R.plurals.core_report_bug_description_field_required,
                    BugReport.DescriptionMinLength, BugReport.DescriptionMinLength
                )
            BugReportValidationError.DescriptionTooLong ->
                binding.bugReportDescriptionLayout to resources.getQuantityString(
                    R.plurals.core_report_bug_form_field_too_long,
                    BugReport.DescriptionMaxLength, BugReport.DescriptionMaxLength
                )
            BugReportValidationError.DescriptionTooShort ->
                binding.bugReportDescriptionLayout to resources.getQuantityString(
                    R.plurals.core_report_bug_description_field_required,
                    BugReport.DescriptionMinLength, BugReport.DescriptionMinLength
                )
            BugReportValidationError.SubjectMissing ->
                binding.bugReportSubjectLayout to getString(R.string.core_report_bug_subject_field_required)
            BugReportValidationError.SubjectTooLong ->
                binding.bugReportSubjectLayout to resources.getQuantityString(
                    R.plurals.core_report_bug_form_field_too_long,
                    BugReport.SubjectMaxLength, BugReport.SubjectMaxLength
                )
        }.exhaustive

        textView.error = message
        scrollTo(textView)
    }

    private fun handleSendingResult(sendingResult: BugReportFormState.SendingResult) {
        when (val result = sendingResult.result) {
            is SendBugReport.Result.Initialized,
            is SendBugReport.Result.Cancelled -> Unit

            is SendBugReport.Result.Failed -> {
                setFormState(isLoading = false)
                binding.root.errorSnack(result.message ?: getString(R.string.core_report_bug_general_error))
            }

            is SendBugReport.Result.Blocked,
            is SendBugReport.Result.Enqueued,
            is SendBugReport.Result.InProgress -> {
                if (input.finishAfterReportIsEnqueued) {
                    reportSuccessfullyCompleted()
                } else Unit
            }

            is SendBugReport.Result.Sent -> reportSuccessfullyCompleted()
        }.exhaustive
    }

    private fun reportSuccessfullyCompleted() {
        setResultOk()
        finish()
    }

    private fun scrollTo(view: View) {
        val rect = Rect()
        binding.scrollContent.offsetDescendantRectToMyCoords(view, rect)
        binding.scrollContent.smoothScrollTo(0, rect.top)
    }

    private fun setFormState(isLoading: Boolean) {
        val sendButton = binding.toolbar.menu.findItem(R.id.bug_report_send)
        val sendingLoader = binding.toolbar.menu.findItem(R.id.bug_report_loader)

        sendButton?.isVisible = !isLoading
        sendingLoader?.isVisible = isLoading
        (sendingLoader?.icon as? AnimatedVectorDrawable)?.let {
            if (isLoading) it.start() else it.stop()
        }

        binding.bugReportDescriptionLayout.isEnabled = !isLoading
        binding.bugReportDescriptionLayout.error = null

        binding.bugReportSubjectLayout.isEnabled = !isLoading
        binding.bugReportSubjectLayout.error = null
    }

    private fun showExitDialog() {
        exitDialog?.dismiss()
        exitDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.core_report_bug_discard_changes_title)
            .setMessage(R.string.core_report_bug_discard_changes_description)
            .setPositiveButton(R.string.core_report_bug_discard_changes_confirm) { _, _ -> viewModel.tryExit(force = true) }
            .setNegativeButton(R.string.core_report_bug_discard_changes_cancel, null)
            .show()
    }

    private fun setResultOk() {
        val data = Intent().apply {
            putExtra(OUTPUT_SUCCESS_MESSAGE, getString(R.string.core_report_bug_success))
        }
        setResult(RESULT_OK, data)
    }

    class ResultContract : ActivityResultContract<BugReportInput, BugReportOutput>() {
        override fun createIntent(context: Context, input: BugReportInput): Intent =
            Intent(context, BugReportActivity::class.java).apply {
                putExtra(INPUT_BUG_REPORT, input)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): BugReportOutput {
            return if (resultCode == RESULT_OK) {
                val message =
                    intent?.getStringExtra(OUTPUT_SUCCESS_MESSAGE) ?: error("Missing `$OUTPUT_SUCCESS_MESSAGE`")
                BugReportOutput.SuccessfullySent(message)
            } else {
                BugReportOutput.Cancelled
            }
        }
    }

    companion object {
        private const val INPUT_BUG_REPORT = "bugReport"
        private const val OUTPUT_SUCCESS_MESSAGE = "successMessage"
    }
}
