package me.proton.core.humanverification.presentation.ui.verification

import android.annotation.SuppressLint
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import me.proton.core.humanverification.presentation.R
import me.proton.core.humanverification.presentation.databinding.FragmentHumanVerificationCaptchaBinding
import me.proton.core.humanverification.presentation.viewmodel.verification.HumanVerificationCaptchaViewModel

/**
 * Created by dinokadrikj on 6/15/20.
 */
class HumanVerificationCaptcha :
    HumanVerificationBaseFragment<HumanVerificationCaptchaViewModel, FragmentHumanVerificationCaptchaBinding>() {

    companion object {

        private const val ARG_HOST = "arg.host"

        operator fun invoke(urlToken: String, host: String): HumanVerificationCaptcha =
            HumanVerificationCaptcha().apply {
                val args = bundleOf(ARG_URL_TOKEN to urlToken, ARG_HOST to host)
                if (arguments != null) requireArguments().putAll(args)
                else arguments = args
            }
    }

    private val host: String by lazy {
        requireArguments().get(ARG_HOST) as String
    }

    private val humanVerificationCaptchaViewModel by viewModels<HumanVerificationCaptchaViewModel>()

    override fun initViewModel() {
        viewModel = humanVerificationCaptchaViewModel
    }

    override fun layoutId(): Int = R.layout.fragment_human_verification_captcha

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated() {
        binding.captchaWebView.apply {
            settings.javaScriptEnabled = true
            addJavascriptInterface(WebAppInterface(), "AndroidInterface")
            webChromeClient = CaptchaWebChromeClient()
        }
        // TODO: check if there is connectivity before doing this below, if there is no, should be notified when there is
        binding.captchaWebView.loadUrl("https://secure.protonmail.com/captcha/captcha.html?token=$urlToken&client=android&host=$host");
    }
    inner class CaptchaWebChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (newProgress == 100 && isAdded) {
                // TODO: if there is a progress loader, remove it and enable user to continue the verification
            }
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun receiveResponse(message: String) {
            verificationToken = message
            // in this moment we should initiate the request for the verification
        }
    }
}
