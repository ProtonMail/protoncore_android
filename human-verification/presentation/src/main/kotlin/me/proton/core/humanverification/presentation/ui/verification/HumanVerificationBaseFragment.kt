package me.proton.core.humanverification.presentation.ui.verification

import android.view.View
import android.widget.EditText
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import me.proton.android.core.presentation.ui.ProtonFragment

/**
 * Created by dinokadrikj on 6/16/20.
 */
abstract class HumanVerificationBaseFragment<VM : ViewModel, DB : ViewDataBinding> : ProtonFragment<VM, DB>() {

    protected val urlToken by lazy {
        requireArguments().get(ARG_URL_TOKEN)
    }

    protected var verificationToken: String? = null

    companion object {
        const val ARG_URL_TOKEN = "arg.urltoken"
    }

    protected open fun editableView(): EditText? = null

    protected fun onClearFieldClicked(view: View) {
        editableView()?.setText("")
    }
}
