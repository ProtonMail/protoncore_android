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

package me.proton.core.reports.presentation.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.proton.core.presentation.ui.ProtonViewBindingActivity
import me.proton.core.presentation.utils.addOnBackPressedCallback
import me.proton.core.presentation.utils.errorSnack
import me.proton.core.presentation.utils.hideKeyboard
import me.proton.core.presentation.utils.showKeyboard
import me.proton.core.report.domain.entity.BugReport
import me.proton.core.report.domain.entity.BugReportValidationError
import me.proton.core.report.domain.usecase.SendBugReport
import me.proton.core.reports.presentation.R
import me.proton.core.reports.presentation.databinding.ActivityBugReportBinding
import me.proton.core.reports.presentation.entity.BugReportFormState
import me.proton.core.reports.presentation.entity.BugReportInput
import me.proton.core.reports.presentation.entity.BugReportOutput
import me.proton.core.reports.presentation.entity.ExitSignal
import me.proton.core.reports.presentation.entity.ReportFormData
import me.proton.core.reports.presentation.viewmodel.BugReportViewModel
import me.proton.core.util.kotlin.exhaustive

@AndroidEntryPoint
internal class BugReportActivity : ProtonViewBindingActivity<ActivityBugReportBinding>(
    ActivityBugReportBinding::inflate
) {
    private var exitDialog: AlertDialog? = null
    private val input: BugReportInput by lazy {
        intent.getParcelableExtra<BugReportInput>(INPUT_BUG_REPORT) ?: error("Missing activity input")
    }
    private var sendButton: MenuItem? = null
    private var sendingLoader: MenuItem? = null
    private val viewModel by viewModels<BugReportViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.bugReportFormState
            .onEach(this::handleBugReportFormState)
            .launchIn(lifecycleScope)

        viewModel.exitSignal
            .onEach(this::handleExitSignal)
            .launchIn(lifecycleScope)

        addOnBackPressedCallback { viewModel.tryExit(getReportFormData()) }

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.spacer.setOnClickListener {
            if (binding.bugReportDescription.hasFocus()) {
                hideKeyboard(binding.bugReportDescription)
            } else {
                showKeyboard(binding.bugReportDescription)
            }
        }
        binding.bugReportDescription.filters = arrayOf(InputFilter.LengthFilter(BugReport.DescriptionMaxLength))
        binding.bugReportSubject.filters = arrayOf(InputFilter.LengthFilter(BugReport.SubjectMaxLength))
        binding.bugReportSubject.requestFocus()
    }

    override fun onDestroy() {
        exitDialog?.dismiss()
        exitDialog = null
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.bug_report_menu, menu)
        sendButton = menu.findItem(R.id.bug_report_send)
        sendingLoader = menu.findItem(R.id.bug_report_loader)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.bug_report_send) {
            viewModel.trySendingBugReport(getReportFormData(), email = input.email, username = input.username)
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun getReportFormData(): ReportFormData {
        return ReportFormData(
            subject = binding.bugReportSubject.text.toString(),
            description = binding.bugReportDescription.text.toString(),
            country = input.country,
            isp = input.isp
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
                binding.bugReportDescriptionLayout to getString(R.string.bug_report_form_field_required)
            BugReportValidationError.SubjectMissing ->
                binding.bugReportSubjectLayout to getString(R.string.bug_report_form_field_required)
            BugReportValidationError.SubjectTooLong ->
                binding.bugReportSubjectLayout to resources.getQuantityString(
                    R.plurals.bug_report_form_field_too_long,
                    BugReport.SubjectMaxLength, BugReport.SubjectMaxLength
                )
            BugReportValidationError.DescriptionTooLong ->
                binding.bugReportDescriptionLayout to resources.getQuantityString(
                    R.plurals.bug_report_form_field_too_long,
                    BugReport.DescriptionMaxLength, BugReport.DescriptionMaxLength
                )
            BugReportValidationError.DescriptionTooShort ->
                binding.bugReportDescriptionLayout to resources.getQuantityString(
                    R.plurals.bug_report_form_field_too_short,
                    BugReport.DescriptionMinLength, BugReport.DescriptionMinLength
                )
        }.exhaustive

        textView.error = message
    }

    private fun handleSendingResult(sendingResult: BugReportFormState.SendingResult) {
        when (val result = sendingResult.result) {
            is SendBugReport.Result.Initialized,
            is SendBugReport.Result.Cancelled -> Unit

            is SendBugReport.Result.Failed -> {
                setFormState(isLoading = false)
                binding.root.errorSnack(result.message ?: getString(R.string.bug_report_general_error))
            }

            is SendBugReport.Result.Blocked,
            is SendBugReport.Result.Enqueued -> {
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

    private fun setFormState(isLoading: Boolean) {
        sendButton?.isVisible = !isLoading
        sendingLoader?.isVisible = isLoading
        (sendingLoader?.icon as? AnimatedVectorDrawable)?.let {
            if (isLoading) it.start() else it.stop()
        }

        binding.bugReportDescriptionLayout.isEnabled = !isLoading
        binding.bugReportDescriptionLayout.error = null

        binding.bugReportSubjectLayout.isEnabled = !isLoading
        binding.bugReportSubjectLayout.error = null

        if (isLoading) hideKeyboard()
    }

    private fun showExitDialog() {
        exitDialog?.dismiss()
        exitDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.bug_report_discard_changes_title)
            .setMessage(R.string.bug_report_discard_changes_description)
            .setPositiveButton(R.string.bug_report_discard_changes_confirm) { _, _ -> viewModel.tryExit(force = true) }
            .setNegativeButton(R.string.bug_report_discard_changes_cancel, null)
            .show()
    }

    private fun setResultOk() {
        val data = Intent().apply {
            putExtra(OUTPUT_SUCCESS_MESSAGE, getString(R.string.bug_report_success))
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
