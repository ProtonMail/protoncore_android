package me.proton.core.auth.presentation.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsCallback
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import androidx.core.os.bundleOf
import androidx.core.util.Consumer
import me.proton.core.auth.domain.feature.IsSsoCustomTabEnabled
import me.proton.core.network.presentation.ui.ProtonWebViewActivity
import me.proton.core.network.presentation.ui.ProtonWebViewActivity.Companion.ResultContract
import me.proton.core.network.presentation.ui.ProtonWebViewActivity.Result
import me.proton.core.presentation.ui.ProtonActivity
import javax.inject.Inject

interface OnWebPageListener {
    fun openWebPageUrl(
        url: String,
        successUrlRegex: String,
        errorUrlRegex: String,
        vararg extraHeaders: Pair<String, String>
    )

    fun onWebPageLoad(errorCode: Int?) {}
    fun onWebPageCancel() {}
    fun onWebPageError() {}
    fun onWebPageSuccess(url: String) {}
}

open class WebPageListenerActivity : OnWebPageListener, ProtonActivity() {

    @Inject
    lateinit var isSsoCustomTabEnabled: IsSsoCustomTabEnabled

    // Handling result from WebView.
    private val webViewLauncher by lazy {
        registerForActivityResult(ResultContract) { result ->
            when (result) {
                null -> onWebPageCancel()
                else -> {
                    onWebPageLoad(result.pageLoadErrorCode)
                    when (result) {
                        is Result.Cancel -> onWebPageCancel()
                        is Result.Error -> onWebPageError()
                        is Result.Success -> onWebPageSuccess(result.url)
                    }
                }
            }
        }
    }

    // Handling result from CustomTabs.
    private val customTabLauncher by lazy {
        registerForActivityResult(StartCustomTab) { result ->
            if (result == false) onWebPageCancel() else Unit
        }
    }

    private var onNewIntentCallback: Consumer<Intent> = Consumer<Intent> { intent ->
        intent?.data?.let { onWebPageSuccess(it.toString()) }
    }
    private var customTabClient: CustomTabsClient? = null
    private var customTabSession: CustomTabsSession? = null
    private var callback: CustomTabsCallback = object : CustomTabsCallback() {
        override fun onNavigationEvent(navigationEvent: Int, extras: Bundle?) {
            when (navigationEvent) {
                NAVIGATION_ABORTED -> onWebPageCancel()
                NAVIGATION_FAILED -> onWebPageError()
                NAVIGATION_FINISHED -> onWebPageLoad(null)
            }
        }
    }

    private val connection: CustomTabsServiceConnection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
            customTabClient = client
            customTabClient?.warmup(0)
            customTabSession = client.newSession(callback)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            customTabClient = null
            customTabSession = null
        }
    }

    private fun bindCustomTabService(context: Context) {
        if (customTabClient != null) return
        val packageName: String = CustomTabsClient.getPackageName(context, null) ?: return
        CustomTabsClient.bindCustomTabsService(context, packageName, connection)
        addOnNewIntentListener(onNewIntentCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindCustomTabService(this)
        checkNotNull(customTabLauncher.contract)
        checkNotNull(webViewLauncher.contract)
    }

    override fun openWebPageUrl(
        url: String,
        successUrlRegex: String,
        errorUrlRegex: String,
        vararg extraHeaders: Pair<String, String>
    ) {
        if (isSsoCustomTabEnabled() && customTabClient != null) {
            customTabLauncher.launch(
                StartCustomTab.Input(
                    url = url,
                    headers = bundleOf(*extraHeaders),
                    session = customTabSession
                )
            )
        } else {
            webViewLauncher.launch(
                ProtonWebViewActivity.Input(
                    url = url,
                    successUrlRegex = successUrlRegex,
                    errorUrlRegex = errorUrlRegex,
                    extraHeaders = mapOf(*extraHeaders),
                    javaScriptEnabled = true,
                    domStorageEnabled = true,
                    shouldOpenLinkInBrowser = false
                )
            )
        }
    }
}
