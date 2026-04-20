package off.kys.sniffer.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import off.kys.sniffer.ui.states.BrowserUiState

class BrowserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    fun onUrlInputChange(newInput: String) {
        _uiState.update { it.copy(urlInput = newInput) }
    }

    fun performSearch() {
        val sanitized = sanitizeUrl(_uiState.value.urlInput)
        _uiState.update { it.copy(currentUrl = sanitized, urlInput = sanitized) }
    }

    fun onProgressChanged(progress: Int) {
        _uiState.update { it.copy(loadingProgress = progress / 100f) }
    }

    fun updateNavigationState(canBack: Boolean, canForward: Boolean) {
        _uiState.update { it.copy(canGoBack = canBack, canGoForward = canForward) }
    }

    fun togglePopups() {
        _uiState.update { it.copy(popupsEnabled = !it.popupsEnabled) }
    }

    fun toggleSheet(visible: Boolean) {
        _uiState.update { it.copy(isSheetVisible = visible) }
    }

    fun addCapturedUrl(url: String) {
        if (!_uiState.value.capturedUrls.contains(url)) {
            _uiState.update { it.copy(capturedUrls = it.capturedUrls + url) }
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