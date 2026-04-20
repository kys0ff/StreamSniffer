package off.kys.sniffer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier

@Composable
fun BrowserBody(
    modifier: Modifier = Modifier,
    currentUrl: String,
    loadingValue: Float,
    onUrlChanged: (String) -> Unit,
    onStreamFound: (String) -> Unit,
    onProgressChanged: (Int) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (loadingValue < 1f) {
            LinearProgressIndicator(
                progress = { loadingValue },
                modifier = Modifier.fillMaxWidth()
            )
        }
        key(currentUrl) {
            SnifferWebView(
                modifier = Modifier.fillMaxSize(),
                url = currentUrl,
                onUrlChanged = onUrlChanged,
                onStreamFound = onStreamFound,
                onProgressChanged = onProgressChanged
            )
        }
    }
}