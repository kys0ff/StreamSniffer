package off.kys.sniffer.ui.components

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import off.kys.sniffer.data.AdBlocker

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SnifferWebView(
    modifier: Modifier = Modifier,
    url: String,
    onUrlChanged: (String) -> Unit = {},
    onStreamFound: (String) -> Unit,
    onProgressChanged: (Int) -> Unit = {}
) {
    val streamExtensions = remember { listOf(".m3u8", ".mpd", ".mp4", ".mkv") }
    val foundStreams = remember { mutableSetOf<String>() }
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChanged(newProgress)
                    }

                    override fun onShowCustomView(
                        view: View?,
                        callback: CustomViewCallback?
                    ) {
                        callback?.onCustomViewHidden()
                    }

                    // Explicitly ignore requests to exit fullscreen
                    override fun onHideCustomView() {}
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        val requestUrl = request?.url?.toString() ?: ""

                        if (AdBlocker.isAd(requestUrl)) {
                            return WebResourceResponse("text/plain", "utf-8", null)
                        }

                        val isStream =
                            streamExtensions.any { requestUrl.contains(it, ignoreCase = true) }
                        if (isStream && !foundStreams.contains(requestUrl)) {
                            foundStreams.add(requestUrl)
                            view?.post { onStreamFound(requestUrl) }
                        }

                        return super.shouldInterceptRequest(view, request)
                    }
                }

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    @Suppress("DEPRECATION")
                    databaseEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    userAgentString =
                        "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }

                loadUrl(url)
                webView = this
            }
        },
        update = {
            if (it.url != url) {
                it.loadUrl(url)
                onUrlChanged(url)
            }
        },
        onRelease = {
            it.destroy()
        }
    )
}