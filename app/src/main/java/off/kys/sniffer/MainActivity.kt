package off.kys.sniffer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.navigator.Navigator
import off.kys.sniffer.data.AdBlocker
import off.kys.sniffer.ui.screens.BrowserScreen
import off.kys.sniffer.ui.theme.StreamSnifferTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LaunchedEffect(key1 = Unit) {
                AdBlocker.loadList()
            }
            StreamSnifferTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Navigator(BrowserScreen())
                }
            }
        }
    }
}