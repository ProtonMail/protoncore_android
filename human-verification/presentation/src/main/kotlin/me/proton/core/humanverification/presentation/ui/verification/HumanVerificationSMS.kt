package me.proton.core.humanverification.presentation.ui.verification

import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationSmsBinding
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationSMSViewModel

/**
 * Created by dinokadrikj on 6/15/20.
 */
class HumanVerificationSMS :
    HumanVerificationBaseFragment<HumanVerificationSMSViewModel, FragmentHumanVerificationSmsBinding>() {

    companion object {
        operator fun invoke(token: String): HumanVerificationSMS = HumanVerificationSMS().apply {
            val args = bundleOf(ARG_URL_TOKEN to token)
            if (arguments != null) requireArguments().putAll(args)
            else arguments = args
        }
    }

    private val humanVerificationSMSViewModel by viewModels<HumanVerificationSMSViewModel>()

    override fun initViewModel() {
        viewModel = humanVerificationSMSViewModel
    }

    override fun onViewCreated() {
        binding.smsEditTextLayout.setEndIconOnClickListener(::onClearFieldClicked)
    }

    override fun editableView(): EditText? = binding.smsEditText

    override fun layoutId(): Int = R.layout.fragment_human_verification_sms

}
