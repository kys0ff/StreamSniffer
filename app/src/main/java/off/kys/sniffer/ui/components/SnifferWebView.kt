package off.kys.sniffer.ui.components

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun SnifferWebView(
    modifier: Modifier = Modifier,
    url: String,
    popupsEnabled: Boolean,
    onViewUpdate: (WebView) -> Unit,
    onStreamFound: (String) -> Unit,
    onProgressChanged: (Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val webViewSavedState = rememberSaveable { mutableStateOf<Bundle?>(null) }

    // Updated states to ensure the callbacks stay fresh inside the WebView
    val currentOnStreamFound by rememberUpdatedState(onStreamFound)
    val currentOnProgressChanged by rememberUpdatedState(onProgressChanged)
    val currentOnViewUpdate by rememberUpdatedState(onViewUpdate)

    val snifferView = remember {
        SnifferView(context).apply {
            webViewSavedState.value?.let { restoreState(it) }
        }
    }

    // Sync callbacks and settings
    snifferView.onStreamFound = currentOnStreamFound
    snifferView.onProgressChanged = currentOnProgressChanged
    snifferView.onViewUpdate = currentOnViewUpdate
    snifferView.setPopupsEnabled(popupsEnabled)

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    snifferView.onResume()
                    snifferView.resumeTimers()
                }
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_DESTROY -> {
                    val bundle = Bundle()
                    snifferView.saveState(bundle)
                    webViewSavedState.value = bundle

                    if (event == Lifecycle.Event.ON_PAUSE) {
                        snifferView.onPause()
                        snifferView.pauseTimers()
                    } else {
                        snifferView.stopLoading()
                        snifferView.destroy()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            snifferView.stopLoading()
        }
    }

    BackHandler(enabled = snifferView.canGoBack()) {
        snifferView.goBack()
    }

    AndroidView(
        modifier = modifier,
        factory = { snifferView },
        update = { view ->
            if (view.url == null && url.isNotBlank()) {
                view.loadUrl(url)
            }
        }
    )
}