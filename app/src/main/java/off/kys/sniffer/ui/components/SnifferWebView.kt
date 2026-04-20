package off.kys.sniffer.ui.components

import android.annotation.SuppressLint
import android.os.Message
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import off.kys.sniffer.data.AdBlocker
import java.util.Collections

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SnifferWebView(
    modifier: Modifier = Modifier,
    url: String,
    onViewUpdate: (WebView) -> Unit,
    onStreamFound: (String) -> Unit,
    onProgressChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val streamExtensions = remember { listOf(".m3u8", ".mpd", ".mp4", ".mkv") }
    // Thread-safe set for background thread interceptions
    val foundStreams = remember { Collections.synchronizedSet(mutableSetOf<String>()) }

    // Maintain latest callbacks for the internal classes
    val currentOnStreamFound by rememberUpdatedState(onStreamFound)
    val currentOnProgressChanged by rememberUpdatedState(onProgressChanged)

    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                    view: WebView?,
                    isDialog: Boolean,
                    isUserGesture: Boolean,
                    resultMsg: Message?
                ): Boolean {
                    // If it's not triggered by a real user click (isUserGesture == false),
                    // it's almost certainly an aggressive ad. Kill it.
                    return isUserGesture
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    currentOnProgressChanged(newProgress)
                }

                override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                    // Consider implementing a real fullscreen toggle here instead of just hiding it
                    callback?.onCustomViewHidden()
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val requestUrl = request?.url?.toString() ?: return null
                    val host = request.url.host ?: ""

                    // runBlocking bridges the non-suspend world to your suspend function
                    val isAd = kotlinx.coroutines.runBlocking {
                        AdBlocker.isAd(host)
                    }

                    if (isAd)
                        return WebResourceResponse("text/plain", "utf-8", null)

                    val isStream =
                        streamExtensions.any { requestUrl.contains(it, ignoreCase = true) }
                    if (isStream && foundStreams.add(requestUrl)) {
                        // Return to main thread to trigger callback
                        view?.post { currentOnStreamFound(requestUrl) }
                    }

                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.let { onViewUpdate(it) }
                }
            }

            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                @Suppress("DEPRECATION")
                databaseEnabled = true
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = false
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
                // Modern UA to avoid "Update your browser" nagging
                userAgentString =
                    "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
        }
    }

    DisposableEffect(webView) {
        onDispose {
            webView.stopLoading()
            webView.destroy()
        }
    }

    BackHandler(enabled = webView.canGoBack()) {
        webView.goBack()
    }

    AndroidView(
        modifier = modifier,
        factory = { webView },
        update = { view ->
            // Only load if the URL actually changed and isn't currently displayed
            if (url.isNotBlank() && view.url != url) {
                view.loadUrl(url)
            }
        }
    )
}