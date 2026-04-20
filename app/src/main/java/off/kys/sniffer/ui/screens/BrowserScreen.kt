package off.kys.sniffer.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import cafe.adriel.voyager.core.screen.Screen
import off.kys.sniffer.ui.components.BrowserBody
import off.kys.sniffer.ui.components.BrowserSearchBar
import off.kys.sniffer.ui.components.StreamBottomSheet
import off.kys.sniffer.ui.components.StreamFab
import off.kys.sniffer.ui.utils.discard

class BrowserScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val capturedUrls = remember { mutableStateListOf<String>() }
        var showSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState()
        var urlInput by remember { mutableStateOf("https://www.google.com") }
        var currentUrl by remember { mutableStateOf("https://www.google.com") }
        val focusManager = LocalFocusManager.current
        var loadingValue by remember { mutableFloatStateOf(0f) }
        var webView by remember { mutableStateOf<WebView?>(null) }
        var canGoBack by remember { mutableStateOf(false) }
        var canGoForward by remember { mutableStateOf(false) }
        var popupsEnabled by remember { mutableStateOf(false) }

        LaunchedEffect(key1 = Unit) {
            currentUrl.discard()
            showSheet.discard()
            webView.discard()
            canGoBack.discard()
            canGoForward.discard()
        }

        Scaffold(
            topBar = {
                BrowserSearchBar(
                    urlInput = urlInput,
                    onUrlChange = { urlInput = it },
                    onSearch = {
                        val sanitized = sanitizeUrl(urlInput)
                        currentUrl = sanitized
                        urlInput = sanitized
                        focusManager.clearFocus()
                    },
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    onBack = { webView?.goBack() },
                    onForward = { webView?.goForward() },
                    popupsEnabled = popupsEnabled,
                    onTogglePopups = { popupsEnabled = !popupsEnabled }
                )
            },
            floatingActionButton = {
                StreamFab(
                    badgeCount = capturedUrls.size,
                    onClick = { showSheet = true }
                )
            }
        ) { contentInsets ->
            BrowserBody(
                modifier = Modifier.padding(contentInsets),
                currentUrl = currentUrl,
                loadingValue = loadingValue,
                onWebViewUpdate = { updatedWebView ->
                    webView = updatedWebView
                    canGoBack = updatedWebView.canGoBack()
                    canGoForward = updatedWebView.canGoForward()
                },
                popupsEnabled = popupsEnabled,
                onStreamFound = { if (!capturedUrls.contains(it)) capturedUrls.add(it) },
                onProgressChanged = { loadingValue = (it / 100).toFloat() }
            )

            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState
                ) {
                    StreamBottomSheet(urls = capturedUrls)
                }
            }
        }
    }

    private fun sanitizeUrl(input: String): String {
        val trimmed = input.trim()
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${trimmed.replace(" ", "+")}"
        }
    }
}