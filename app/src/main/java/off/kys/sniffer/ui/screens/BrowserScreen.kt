package off.kys.sniffer.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import cafe.adriel.voyager.core.screen.Screen
import off.kys.sniffer.di.AppContainer
import off.kys.sniffer.ui.components.BrowserBody
import off.kys.sniffer.ui.components.BrowserSearchBar
import off.kys.sniffer.ui.components.StreamBottomSheet
import off.kys.sniffer.ui.components.StreamFab
import off.kys.sniffer.ui.utils.discard
import off.kys.sniffer.ui.viewmodels.BrowserViewModel

class BrowserScreen(
    private val viewModel: BrowserViewModel = AppContainer.browserViewModel
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val state by viewModel.uiState.collectAsState()

        val focusManager = LocalFocusManager.current
        val sheetState = rememberModalBottomSheetState()
        var webView by remember { mutableStateOf<WebView?>(null) }

        Scaffold(
            topBar = {
                BrowserSearchBar(
                    urlInput = state.urlInput,
                    onUrlChange = viewModel::onUrlInputChange,
                    onSearch = {
                        viewModel.performSearch()
                        focusManager.clearFocus()
                    },
                    canGoBack = state.canGoBack,
                    canGoForward = state.canGoForward,
                    onBack = { webView?.goBack() },
                    onForward = { webView?.goForward() },
                    popupsEnabled = state.popupsEnabled,
                    onTogglePopups = viewModel::togglePopups
                )
            },
            floatingActionButton = {
                StreamFab(
                    badgeCount = state.capturedUrls.size,
                    onClick = { viewModel.toggleSheet(true) }
                )
            }
        ) { contentInsets ->
            BrowserBody(
                modifier = Modifier.padding(contentInsets),
                currentUrl = state.currentUrl,
                loadingValue = state.loadingProgress,
                onWebViewUpdate = { updatedWebView ->
                    webView = updatedWebView
                    webView.discard()
                    viewModel.updateNavigationState(
                        canBack = updatedWebView.canGoBack(),
                        canForward = updatedWebView.canGoForward()
                    )
                },
                popupsEnabled = state.popupsEnabled,
                onStreamFound = viewModel::addCapturedUrl,
                onProgressChanged = viewModel::onProgressChanged
            )

            if (state.isSheetVisible) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleSheet(false) },
                    sheetState = sheetState
                ) {
                    StreamBottomSheet(urls = state.capturedUrls)
                }
            }
        }
    }
}