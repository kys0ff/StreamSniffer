package off.kys.sniffer.ui.states

data class BrowserUiState(
    val urlInput: String = "https://www.google.com",
    val currentUrl: String = "https://www.google.com",
    val capturedUrls: List<String> = emptyList(),
    val loadingProgress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val popupsEnabled: Boolean = false,
    val isSheetVisible: Boolean = false
)