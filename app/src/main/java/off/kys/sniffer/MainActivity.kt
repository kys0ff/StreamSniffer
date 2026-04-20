package off.kys.sniffer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cafe.adriel.voyager.navigator.Navigator
import off.kys.sniffer.ui.screens.BrowserScreen
import off.kys.sniffer.ui.theme.StreamSnifferTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StreamSnifferTheme {
                Navigator(BrowserScreen())
            }
        }
    }
}