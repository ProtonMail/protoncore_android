package me.proton.core.humanverification.presentation.ui.verification

import android.view.View
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationEmailBinding
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationEmailViewModel

/**
 * Created by dinokadrikj on 6/15/20.
 */
class HumanVerificationEmail :
    HumanVerificationBaseFragment<HumanVerificationEmailViewModel, FragmentHumanVerificationEmailBinding>() {

    companion object {
        operator fun invoke(token: String): HumanVerificationEmail =
            HumanVerificationEmail().apply {
                val args = bundleOf(ARG_URL_TOKEN to token)
                if (arguments != null) requireArguments().putAll(args)
                else arguments = args
            }
    }

    private val humanVerificationEmailViewModel by viewModels<HumanVerificationEmailViewModel>()

    override fun initViewModel() {
        viewModel = humanVerificationEmailViewModel
    }

    override fun onViewCreated() {
        binding.emailEditTextLayout.setEndIconOnClickListener(::onClearFieldClicked)
    }

    override fun editableView(): EditText? = binding.emailEditText

    override fun layoutId(): Int = R.layout.fragment_human_verification_email
}
