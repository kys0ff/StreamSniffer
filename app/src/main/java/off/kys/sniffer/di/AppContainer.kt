package off.kys.sniffer.di

import off.kys.sniffer.ui.viewmodels.BrowserViewModel

object AppContainer {
     val browserViewModel by lazy { BrowserViewModel() }
}