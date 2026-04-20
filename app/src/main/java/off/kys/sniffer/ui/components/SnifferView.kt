package off.kys.sniffer.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.runBlocking
import off.kys.sniffer.data.AdBlocker
import java.util.Collections

@Suppress("DEPRECATION")
@SuppressLint("SetJavaScriptEnabled")
class SnifferView(context: Context) : WebView(context) {

    var onStreamFound: ((String) -> Unit)? = null
    var onProgressChanged: ((Int) -> Unit)? = null
    var onViewUpdate: ((WebView) -> Unit)? = null

    private val streamExtensions = listOf(".m3u8", ".mpd", ".mp4", ".mkv")
    private val foundStreams = Collections.synchronizedSet(mutableSetOf<String>())

    private val blockedKeywords = listOf(
        "bet", "casino", "slot", "poker", "shop", "store", "marketplace",
        "1xbet", "aliexpress", "temu", "u.tk", "koko5000fh", "q1ayxwi7",
        "tukrd.com", "ijline.com", "29611601-", "oundhertobeconsist",
        "crmkt.livejasmine", "rirki.com", ".bar/"
    )

    init {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                onProgressChanged?.invoke(newProgress)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                callback?.onCustomViewHidden()
            }
        }

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val host = request?.url?.host?.lowercase() ?: ""
                return blockedKeywords.any { host.contains(it) }
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val requestUrl = request?.url?.toString() ?: return null
                val host = request.url.host ?: ""

                // Manual keyword check first (fastest)
                if (blockedKeywords.any { host.contains(it) }) {
                    return WebResourceResponse("text/plain", "utf-8", null)
                }

                // Bridge to coroutines because WebViewClient is allergic to 'suspend'
                val isAd = runBlocking {
                    AdBlocker.isAd(host)
                }

                if (isAd) {
                    return WebResourceResponse("text/plain", "utf-8", null)
                }

                val isStream = streamExtensions.any { requestUrl.contains(it, ignoreCase = true) }
                if (isStream && foundStreams.add(requestUrl)) {
                    post { onStreamFound?.invoke(requestUrl) }
                }

                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.let { onViewUpdate?.invoke(it) }
            }
        }

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            useWideViewPort = true
            loadWithOverviewMode = true
            userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }
    }

    fun setPopupsEnabled(enabled: Boolean) {
        settings.javaScriptCanOpenWindowsAutomatically = enabled
    }
}